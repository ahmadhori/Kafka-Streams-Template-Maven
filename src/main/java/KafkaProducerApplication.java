import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class KafkaProducerApplication {

    public static void main(String[] args) throws Exception {
        log.info("Starting Kafka Random Producer Application");

        Properties props = new Properties();
        try (InputStream inputStream = new FileInputStream("configuration/dev.properties")) {
            props.load(inputStream);
        }

        final String inputTopic = props.getProperty("input.topic.name");

        Util utility = new Util();
        Util.Randomizer producer1 = utility.startNewRandomizer(props, inputTopic);
    }
}