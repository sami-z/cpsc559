package Kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ResponseQueueProducer {
    KafkaProducer<String, String> producer;
    private final Properties properties;

    public ResponseQueueProducer() { properties = new Properties(); }

    private void setProperties() {
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());
    }

    private void createProducerInstance() {
        producer = new KafkaProducer<>(properties);
    }

    private ProducerRecord<String, String> createProducerRecord(String topic, String message) {
        return new ProducerRecord<>(topic, message);
    }

    public void sendMessages(String topic, String message) {
        setProperties();
        createProducerInstance();

        ProducerRecord<String, String> producerRecord = createProducerRecord(topic, message);

        // send data -- asynchronous
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception e) {
                // executed every time a record successfully sent or an exception is thrown
                if (e == null) {
                    // the record was successfully sent
                    System.out.println("Received new metadata \n" +
                            "Topic: " + metadata.topic() + "\n" +
                            "Partition: " + metadata.partition() + "\n" +
                            "Offset: " + metadata.offset() + "\n" +
                            "Timestamp: " + metadata.timestamp());
                } else {
                    System.out.println("Error while producing");
                    e.printStackTrace();
                }
            }
        });
    }
}
