package RequestQueue.Server;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;

@SpringBootApplication
@ComponentScan({ "RequestQueue" })
public class RequestQueueServerMain {
    public static void main(String[] args) throws IOException {

        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        System.out.println(ip);

        LeaderState.serverIP = ip;
        LeaderState.leaderIP = "";

        SpringApplication app = new SpringApplication(RequestQueueServerMain.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.REQUEST_QUEUE_PORT)));

        ApplicationContext context = app.run(args);
        RequestQueueHandler requestQueueMicroService = context.getBean(RequestQueueHandler.class);
        FileQueue fq = context.getBean(FileQueue.class);

        RequestQueueWebServer requestQueueWebServer = new RequestQueueWebServer(NetworkConstants.REQUEST_QUEUE_SOCKET_PORT, requestQueueMicroService,fq);
        Thread requestQueueWebServerThread = new Thread(requestQueueWebServer);
        requestQueueWebServerThread.start();
        System.out.println("Request queue web server is running...");
    }
}
