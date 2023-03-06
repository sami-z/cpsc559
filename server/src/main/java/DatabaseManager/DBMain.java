package DatabaseManager;

import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.UnknownHostException;
import java.util.Collections;

public class DBMain {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(DatabaseController.class);
        System.out.println("DATABASE MANAGER IS RUNNING");
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.DATABASE_MANAGER_PORT)));
        app.run(args);
    }
}
