package RequestQueue;

import java.net.UnknownHostException;

public class RequestQueueServerMain {
    public static void main(String[] args) throws UnknownHostException {
        RequestQueue requestQueue = new RequestQueue();

        RequestQueueServer requestQueueServer = new RequestQueueServer(1000, requestQueue);
        Thread requestQueueServerThread = new Thread(requestQueueServer);
        requestQueueServerThread.start();
        System.out.println("Request queue server is running...");

        RequestQueueWebServer requestQueueWebServer = new RequestQueueWebServer(60, requestQueue);
        Thread requestQueueWebServerThread = new Thread(requestQueueWebServer);
        requestQueueWebServerThread.start();
        System.out.println("Request queue web server is running...");

        while (true) {

        }
    }
}
