import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class Util implements AutoCloseable {

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    public class Randomizer implements AutoCloseable, Runnable {
        private Properties props;
        private String topic;
        private Producer<String, String> producer;
        private boolean closed;

        public Randomizer(Properties producerProps, String topic) {
            this.closed = false;
            this.topic = topic;
            this.props = producerProps;
            this.props.setProperty("client.id", "faker");
        }

        public void run() {
            try (KafkaProducer producer = new KafkaProducer<String, String>(props)) {
                Faker faker = new Faker();
                while (!closed) {
                    log.info("Sending a new message to topic {}", topic);
                    try {
                        Object result = producer.send(new ProducerRecord<>(
                                this.topic,
                                faker.chuckNorris().fact())).get();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException", e);
                    }
                }
            } catch (Exception ex) {
                log.error(ex.toString());
            }
        }
        public void close()  {
            closed = true;
        }
    }

    public Randomizer startNewRandomizer(Properties producerProps, String topic) {
        Randomizer rv = new Randomizer(producerProps, topic);
        executorService.submit(rv);
        return rv;
    }

    public void createTopics(final Properties allProps, List<NewTopic> topics)
            throws InterruptedException, ExecutionException, TimeoutException {
        try (final AdminClient client = AdminClient.create(allProps)) {
            log.info("Creating topics");
            CreateTopicsResult result = client.createTopics(topics);
            client.createTopics(topics).values().forEach( (topic, future) -> {
                try {
                    future.get();
                } catch (Exception ex) {
                    log.info(ex.toString());
                }
            });

            Collection<String> topicNames = topics
                    .stream()
                    .map(t -> t.name())
                    .collect(Collectors.toCollection(LinkedList::new));

            log.info("Asking cluster for topic descriptions");
            client
                    .describeTopics(topicNames)
                    .all()
                    .get(10, TimeUnit.SECONDS)
                    .forEach((name, description) -> log.info("Topic Description: {}", description.toString()));
        }
    }

    public void close() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }
}