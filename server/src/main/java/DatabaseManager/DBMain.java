package DatabaseManager;

import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.util.Collections;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class DBMain {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DBMain.class);
        System.out.println("DATABASE MANAGER IS RUNNING");
        DBManagerState.DBLeaderIP = "";
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.DATABASE_MANAGER_PORT)));
        app.run(args);
    }
}
