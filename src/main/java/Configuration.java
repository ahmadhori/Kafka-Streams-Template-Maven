import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;


import java.util.Properties;

import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;


@Slf4j
public class Configuration {

    private final CommandLine cmd;
    private final Properties kafkaProperties;
    private final Properties kafkaStreamsProperties;
    private final Properties applicationProperties;

    private Configuration(CommandLine cmd, Properties kafkaProperties, Properties applicationProperties, Properties kafkaStreamsProperties) {
        this.cmd = cmd;
        this.kafkaProperties = kafkaProperties;
        this.applicationProperties = applicationProperties;
        this.kafkaStreamsProperties = kafkaStreamsProperties;
    }

    public static Configuration from(String[] args) {
        // Parsing command line arguments
        Options options = new Options();
        options.addOption(option("ENV"));
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }


        PropertiesConfiguration propertiesConfiguration;
        try {
            Configurations configs = new Configurations();
            String configFileName = "config-" + cmd.getOptionValue("ENV", "local") + ".properties";
            propertiesConfiguration = configs.properties(configFileName);
        } catch (ConfigurationException cex) {
            // Something went wrong
            throw new RuntimeException(cex.getMessage(), cex);
        }


        Properties kafka_props = getProperties(propertiesConfiguration.subset("kafka"));
        Properties app_props = getProperties(propertiesConfiguration.subset("app"));
        Properties kafkastreams_props = getProperties(propertiesConfiguration.subset("kafkastreams"));

        return new Configuration(cmd, kafka_props, app_props, kafkastreams_props);
    }

    private static Option option(String name) {
        return Option.builder().longOpt(name)
                .hasArg(true).required(false).numberOfArgs(1)
                .optionalArg(true).build();
    }


    public String getInputTopicName() {
        return applicationProperties.getProperty("input.topic.name");
    }

    public String getOutputTopicName() {
        return applicationProperties.getProperty("output.topic.name");
    }

    public Properties getProducerProps() {
        return kafkaProperties;
    }

    public Properties getKafkaStreamsProperties() {
        return kafkaStreamsProperties;
    }
}
