package ExecutionCore;

import DBServ.DB;
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
            rqSocket = new Socket(NetworkConstants.REQUEST_QUEUE_IP,NetworkConstants.REQUEST_SERVER_SOCKET_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DB db = new DB();

        new Thread(new ElectionHandler()).start();

        while(this.isRunning){
            try {
                OutputStream output = rqSocket.getOutputStream();
                output.write(NetworkConstants.PING_VALUE);
                System.out.println("aaa");
                ExecutionCoreHandler.processEvent(rqSocket,db);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            rqSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server Stopped.") ;
        System.out.println("Closing server");
    }
}