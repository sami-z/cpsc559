package ExecutionCore;

import Util.NetworkConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadedServer implements Runnable{
    ServerSocket serverSocket = null;
    boolean isRunning = false;

    public synchronized void stop() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;
        Socket rqSocket = null;
        try {
            rqSocket = new Socket(NetworkConstants.REQUEST_QUEUE_IP,NetworkConstants.REQUEST_QUEUE_SOCKET_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(this.isRunning){
            try {
                OutputStream output = rqSocket.getOutputStream();
                output.write(NetworkConstants.PING_VALUE);

                ExecutionCoreHandler.processEvent(rqSocket,db);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        rqSocket.close();
        System.out.println("Server Stopped.") ;
        System.out.println("Closing server");
    }
}