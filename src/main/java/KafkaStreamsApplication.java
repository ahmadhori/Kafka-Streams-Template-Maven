import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaStreamsApplication {

    static void runKafkaStreams(final KafkaStreams streams) {
        final CountDownLatch latch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (oldState == KafkaStreams.State.RUNNING && newState != KafkaStreams.State.RUNNING) {
                latch.countDown();
            }
        });

        streams.start();

        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Streams Closed");
    }

    static Topology buildTopology(String inputTopic, String outputTopic) {
        Serde<String> stringSerde = Serdes.String();

        StreamsBuilder builder = new StreamsBuilder();

        builder
                .stream(inputTopic, Consumed.with(stringSerde, stringSerde))
                .peek((k, v) -> log.info("Observed event: {}", v))
                .mapValues(s -> s.toUpperCase())
                .peek((k, v) -> log.info("Transformed event: {}", v))
                .to(outputTopic, Produced.with(stringSerde, stringSerde));

        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting Kafka Streams Application");

        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("configuration/dev.properties")) {
            props.load(inputStream);
        }

        final String inputTopic = props.getProperty("input.topic.name");
        final String outputTopic = props.getProperty("output.topic.name");


        // Ramdomizer only used to produce sample data for this application, not typical usage

        KafkaStreams kafkaStreams = new KafkaStreams(buildTopology(inputTopic, outputTopic), props);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        log.info("Kafka Streams 101 App Started");
        runKafkaStreams(kafkaStreams);
    }
}