package Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
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
    }

    private void setProperties() {
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("auto.offset.reset", "earliest");
    }

    private void setPartition() {
        // subscribe to a topic
        String topic = "test1";
        int numPartitions = 1;

        List<TopicPartition> partitions = Collections.singletonList(new TopicPartition(topic, numPartitions));
        consumer.assign(partitions);
    }

    private void createConsumerInstance() {
        consumer = new KafkaConsumer<>(properties);
    }

    public ConsumerRecord<String,String> getMessages() {
        setProperties();
        createConsumerInstance();

        // get a reference to the main thread
        final Thread mainThread = Thread.currentThread();

        // adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Detected a shutdown. Exit by calling consumer.wakeup()");
                consumer.wakeup();

                // join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            setPartition();

            // poll for da
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record: records) {
                    return record;
                }

                return null;
        } catch (WakeupException e) {
            System.out.println("Consumer is starting to shut down");
        } catch (Exception e) {
            System.out.println("Unexpected exception in the consumer");
        } finally {
            consumer.close(); // close the consumer, this will also commit offsets
            System.out.println("The consumer is now gracefully shut down");
            return null;
        }
    }
}

