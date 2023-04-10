package DatabaseManager;

import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.util.Collections;

/**

 The main class for running the database manager application. This class is annotated with the @SpringBootApplication
 annotation to enable Spring Boot autoconfiguration. It also excludes the MongoAutoConfiguration and MongoDataAutoConfiguration
 classes to prevent Spring Boot from automatically configuring MongoDB.
 The main method sets the default properties for the application, including the server port, and starts the application using
 the SpringApplication class. The method also initializes the DBManagerState.DBLeaderIP to an empty string.
 */
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
