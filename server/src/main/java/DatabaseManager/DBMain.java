package DatabaseManager;

import DatabaseManager.API.DatabaseController;
import DatabaseManager.Service.DatabaseHandler;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.net.UnknownHostException;
import java.util.Collections;

@SpringBootApplication
public class DBMain {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(DBMain.class);
        System.out.println("DATABASE MANAGER IS RUNNING");
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.DATABASE_MANAGER_PORT)));
        app.run(args);

//        new Thread(new DatabaseClusterMonitor()).start();
    }
}
