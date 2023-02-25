package Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class RequestQueueConsumer {

//    private static final Logger log = LoggerFactory.getLogger(Consumer.class.getSimpleName());
    private KafkaConsumer<String, String> consumer;
    private final Properties properties;

    public RequestQueueConsumer() {
        properties = new Properties();
        setProperties();
        createConsumerInstance();
        setPartition();
    }

    private void setProperties() {
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty("auto.offset.reset", "earliest");
        properties.setProperty("max.poll.records", "1");
    }

    private void setPartition() {
        // subscribe to a topic
        String topic = "test1";
        int numPartitions = 0;

        List<TopicPartition> partitions = Collections.singletonList(new TopicPartition(topic, numPartitions));
        consumer.assign(partitions);
    }

    private void createConsumerInstance() {
        consumer = new KafkaConsumer<>(properties);
    }

    public void closeConsumer() {
        consumer.close();
    }

    public ConsumerRecord<String,String> getMessages() {
        try {
            // poll for da
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records) {
                return record;
            }

            return null;
        } catch (Exception e) {
            System.out.println("Unexpected exception in the consumer");
            e.printStackTrace();
        } finally {
            return null;
        }
    }
}

