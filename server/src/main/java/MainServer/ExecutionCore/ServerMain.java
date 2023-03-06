package MainServer.ExecutionCore;

import DatabaseManager.DatabaseController;
import MainServer.ElectionCore.ElectionController;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

public class ServerMain {

    public static void main(String[] args) throws UnknownHostException {
        InetAddress IP=InetAddress.getLocalHost();

        SpringApplication app = new SpringApplication(ElectionController.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.MAIN_SERVER_PORT)));
        app.run(args);

        System.out.println("MAIN SERVER IS RUNNING");
        new Thread(new MultiThreadedServer()).start();


    }
}
