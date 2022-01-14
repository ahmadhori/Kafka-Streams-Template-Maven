import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class KafkaProducerApplication {

    public static void main(String[] args) throws Exception {
        log.info("Starting Kafka Random Producer Application");
        final Configuration configuration = Configuration.from(args);
        final String inputTopic = configuration.getInputTopicName();

        Util utility = new Util();
        Util.Randomizer producer1 = utility.startNewRandomizer(configuration.getProducerProps(), inputTopic);
    }
}