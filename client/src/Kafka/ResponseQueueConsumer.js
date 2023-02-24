const { Kafka } = require("kafkajs");

class ResponseQueueConsumer {
    constructor() {
        this.consumer = null;
        this.properties = {
            "clientId": "response-consumer",
            "brokers": ["localhost:9092"],
            "groupId": "response-group",
            "autoOffsetReset": "earliest",
        };
    }

    async createConsumerInstance() {
        const kafka = new Kafka({ "brokers": this.properties.brokers });
        this.consumer = kafka.consumer({ "groupId": this.properties.groupId });
        await this.consumer.connect();
    }

    async setPartition() {
        // subscribe to a topic
        const topic = "test1";
        const numPartitions = 1;
        await this.consumer.subscribe({ "topic": topic, "fromBeginning": true });
    }

    async getMessages() {
        await this.createConsumerInstance();
        await this.setPartition();

        // adding the shutdown hook
        process.on("SIGINT", async () => {
            console.log("Detected a shutdown. Exit by calling consumer.disconnect()");
            await this.consumer.disconnect();
        });

        await this.consumer.run({
            "eachMessage": async ({ topic, partition, message }) => {
                console.log({
                    "key": message.key.toString(),
                    "value": message.value.toString(),
                    "partition": partition,
                    "offset": message.offset,
                });
            },
        });
    }
}

/*
* Below is how to instantiate the consumer and retrieve messages from the Kafka queue */

// const consumer = new ResponseQueueConsumer();
// consumer.getMessages().catch(console.error);