package RequestQueue;

import java.io.IOException;
import java.net.*;

public class RequestQueueServer implements Runnable{
    private final int requestQueueServerPort;
    private boolean isRunning;
    private ServerSocket requestQueueServerSocket = null;
    private Socket clientSocket = null;
    private RequestQueue requestQueue;

    public RequestQueueServer(int portNumber) {
        this.requestQueueServerPort = portNumber;
        this.requestQueue = new RequestQueue();
    }

    private void openRequestQueueServerSocket() {
        try {
            requestQueueServerSocket = new ServerSocket(requestQueueServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptClientSocket() {
        try {
            clientSocket = requestQueueServerSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        this.openRequestQueueServerSocket();
        this.acceptClientSocket();

        while (isRunning) {
            new Thread( new RequestQueueHandler(clientSocket, requestQueue)).start();
        }

        System.out.println("RequestQueue is closing");
    }
}
