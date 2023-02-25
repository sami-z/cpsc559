package RequestQueue;

import java.io.IOException;
import java.net.*;
import java.sql.SQLOutput;

public class RequestQueueServer {
    private final int requestQueueServerPort;
    ServerSocket requestQueueServerSocket = null;

    public RequestQueueServer(int portNumber) {
        this.requestQueueServerPort = portNumber;
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
            Socket clientSocket = requestQueueServerSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runServer() {
        this.openRequestQueueServerSocket();
        this.acceptClientSocket();

        while (true) {
            // RequestQueue instance and do a bunch of stuff here
        }
    }
}
