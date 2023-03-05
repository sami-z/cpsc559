package RequestQueue;

import RequestQueue.DataAccessObject.RequestQueue;
import RequestQueue.Service.RequestQueueHandler;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.net.UnknownHostException;

@SpringBootApplication
public class RequestQueueServerMain {
    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext context = SpringApplication.run(RequestQueueServerMain.class, args);
        RequestQueueHandler requestQueueMicroService = context.getBean(RequestQueueHandler.class);
//        RequestQueue requestQueue = new RequestQueue();
//
//        RequestQueueServer requestQueueServer = new RequestQueueServer(NetworkConstants.REQUEST_SERVER_SOCKET_PORT, requestQueue);
//        Thread requestQueueServerThread = new Thread(requestQueueServer);
//        requestQueueServerThread.start();
//        System.out.println("Request queue server is running...");
//
        RequestQueueWebServer requestQueueWebServer = new RequestQueueWebServer(NetworkConstants.REQUEST_QUEUE_SOCKET_PORT, requestQueueMicroService);
        Thread requestQueueWebServerThread = new Thread(requestQueueWebServer);
        requestQueueWebServerThread.start();
        System.out.println("Request queue web server is running...");
    }
}
