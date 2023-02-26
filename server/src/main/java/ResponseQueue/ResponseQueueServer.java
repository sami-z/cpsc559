package ResponseQueue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ResponseQueueServer implements Runnable{

    private int port;
    private boolean running;
    private ServerSocket serverSocket;
    private ResponseQueue rq;

    public ResponseQueueServer(int port, ResponseQueue rq){
        this.port = port;
        this.rq = rq;
    }


    private synchronized void openServerSocket() {
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {

        }
    }


    @Override
    public void run() {
        this.running = true;
        this.openServerSocket();

        while(this.running){
            Socket cs = null;
            try {
                cs = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread( new ResponseQueueHandler(cs,rq)).start();
        }

        System.out.println("ResponseQueue is closing");
    }
}
