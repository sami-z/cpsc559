package RequestQueue;

import java.io.IOException;
import java.net.*;

public class RequestQueueServer implements Runnable{
    private final int requestQueueServerPort;
    private boolean isRunning;
    private ServerSocket requestQueueServerSocket = null;
    private Socket clientSocket = null;
    private final RequestQueue requestQueue;

    public RequestQueueServer(int portNumber, RequestQueue requestQueue) {
        this.requestQueueServerPort = portNumber;
        this.requestQueue = requestQueue;
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
        this.isRunning = true;
        this.openRequestQueueServerSocket();

        while (isRunning) {
            this.acceptClientSocket();
            new Thread( new RequestQueueHandler(clientSocket, requestQueue)).start();
        }

        try {
            requestQueueServerSocket.close();
            System.out.println("RequestQueue is closing");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
