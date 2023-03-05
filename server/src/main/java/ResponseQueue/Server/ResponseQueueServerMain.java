package ResponseQueue.Server;

import ResponseQueue.Service.ResponseQueueHandler;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.net.UnknownHostException;

@SpringBootApplication
public class ResponseQueueServerMain {
    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext context = SpringApplication.run(ResponseQueueServerMain.class, args);
        ResponseQueueHandler responseQueueMicroService = context.getBean(ResponseQueueHandler.class);

        ResponseQueueWebServer responseQueueWebServer = new ResponseQueueWebServer(NetworkConstants.RESPONSE_QUEUE_SERVER_PORT, responseQueueMicroService);
        Thread responseQueueWebServerThread = new Thread(responseQueueWebServer);
        responseQueueWebServerThread.start();
        System.out.println("Response queue web server is running...");
    }
}
