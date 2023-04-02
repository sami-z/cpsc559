package DatabaseManager;

import MainServer.Monitor.DBManagerMonitor;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections;

@SpringBootApplication
public class DBMain {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DBMain.class);
        System.out.println("DATABASE MANAGER IS RUNNING");
        DBManagerState.DBLeaderIP = "";
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.DATABASE_MANAGER_PORT)));
        app.run(args);

        new Thread(new DatabaseClusterMonitor()).start();
    }
}
