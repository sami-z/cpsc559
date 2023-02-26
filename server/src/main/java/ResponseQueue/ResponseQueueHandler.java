package ResponseQueue;

import java.net.Socket;

public class ResponseQueueHandler implements Runnable{

    private Socket clientSocket;
    private ResponseQueue rq;

    public ResponseQueueHandler(Socket clientSocket, ResponseQueue rq){
        this.clientSocket = clientSocket;
        this.rq = rq;
    }

    @Override
    public void run() {
        
    }
}
