# Kafka Streams Template Maven Project  

This project will be used to create the followings:  
- **A Kafka Producer Application** that will start producing random messages to a kafka topic
- **A Kafka Streams application** that will consume messages from the previous Kafka topic and produce a new message to the output-topic.  


We will use **Confluent Kafka Binaries** to run the Kafka cluster.

## Setup a Kafka cluster:
- Download the binaries from [confluent kafka download page](https://www.confluent.io/download/).
- Navigate to the directory where the binaries are located in my case they are located in:  
  `~/confluent-7.0.1`
- Add the following to the `~/.bash_profile` or `~/.zshrc` file depending on your default shell.

    ```bash
    export CONFLUENT_HOME=~/confluent-7.0.1  
    export PATH=$CONFLUENT_HOME/bin:$PATH
    ```

Now you are ready to start using kafka.

- Start zookeeper server in separate window.  
  by default the zookeeper server starts on port 2181.

    ```bash
    zookeeper-server-start etc/kafka/zookeeper.properties
    ```

- Start the kafka server in separate window.  
  by default the kafka server starts on port 9092.

    ```bash
    kafka-server-start etc/kafka/server.properties
    ```

- Create a topic.

    ```bash
    kafka-topics --create --topic messages-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
    ```

- List available Kafka topics.

    ```bash
    kafka-topics --list --bootstrap-server localhost:9092
    ```



## Create a Kafka Producer Application

- Run the first part of the application which is KafkaProducerApplication and this small app will start producing messages in the topic messages-topic.

- Start a kafka console consumer on the messages-topic in separate window to check the results.

    ```bash
    kafka-console-consumer --topic messages-topic --bootstrap-server localhost:9092
    ```


## Create a Kafka Streams Application

- Run the second part of the application which is KafkaStreamsApplication.  
This small app will use *Kafka Streams* to apply some transformations on the messages in the topic messages-topic, and it will produce the result messages in the topic output-topic

- Start a kafka console consumer on the output-topic in separate window to check the results of the kafka-stream-app.
    
    ```bash
    kafka-console-consumer --topic output-topic --bootstrap-server localhost:9092
    ```