package ExecutionCore;

import Kafka.RequestQueueConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.net.ServerSocket;
        import java.net.Socket;

public class MultiThreadedServer implements Runnable{
    int serverPort = 8080;
    ServerSocket serverSocket = null;
    boolean isRunning = false;


    public MultiThreadedServer(int port) {
        this.serverPort = port;
    }

    public synchronized void stop() {
        this.isRunning = false;
    }

    private synchronized void openServerSocket() {
        try {
            serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        this.isRunning = true;
        this.openServerSocket();
        RequestQueueConsumer consumer = new RequestQueueConsumer();

        while(this.isRunning){
            ConsumerRecord<String, String> currMessage;
            currMessage = consumer.getMessages();

            if(currMessage != null) {
                new Thread(
                        new HandlerThread(currMessage.value())).start();
            }
        }
        consumer.closeConsumer();
        System.out.println("Server Stopped.") ;

        System.out.println("Closing server");
    }
}