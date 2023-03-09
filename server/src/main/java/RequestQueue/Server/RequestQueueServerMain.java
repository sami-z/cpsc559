package RequestQueue.Server;

import RequestQueue.Service.RequestQueueHandler;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.net.UnknownHostException;
import java.util.Collections;

@SpringBootApplication
@ComponentScan({ "RequestQueue" })
public class RequestQueueServerMain {
    public static void main(String[] args) throws UnknownHostException {

        SpringApplication app = new SpringApplication(RequestQueueServerMain.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.REQUEST_QUEUE_PORT)));

        ApplicationContext context = app.run(args);
        RequestQueueHandler requestQueueMicroService = context.getBean(RequestQueueHandler.class);

        RequestQueueWebServer requestQueueWebServer = new RequestQueueWebServer(NetworkConstants.REQUEST_QUEUE_SOCKET_PORT, requestQueueMicroService);
        Thread requestQueueWebServerThread = new Thread(requestQueueWebServer);
        requestQueueWebServerThread.start();
        System.out.println("Request queue web server is running...");
    }
}
