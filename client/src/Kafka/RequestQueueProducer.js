const { Kafka } = require('kafkajs');

class RequestQueueProducer {
    constructor() {
        this.producer = null;
        this.properties = {
            'bootstrap.servers': '127.0.0.1:9092',
            'key.serializer': 'org.apache.kafka.common.serialization.StringSerializer',
            'value.serializer': 'org.apache.kafka.common.serialization.StringSerializer'
        };
    }

    async createProducerInstance() {
        const kafka = new Kafka({ brokers: [this.properties['bootstrap.servers']] });
        this.producer = kafka.producer();
        await this.producer.connect();
    }

    createProducerRecord(topic, message) {
        return { topic: topic, messages: message };
    }

    async sendMessages(topic, message) {
        await this.createProducerInstance();

        const producerRecord = this.createProducerRecord(topic, message);

        // send data -- asynchronous
        const result = await this.producer.send({
            topic: producerRecord.topic,
            messages: [{ value: producerRecord.messages }]
        });

        console.log(`Received new metadata:\n${JSON.stringify(result, null, 4)}`);

        await this.producer.disconnect();
    }
}

/*
* Below is how to instantiate the producer and send messages to the Kafka queue */

// const requestQueueProducer = new RequestQueueProducer();
// requestQueueProducer.sendMessages('requestTest1', 'This is a test message being sent to the request queue');