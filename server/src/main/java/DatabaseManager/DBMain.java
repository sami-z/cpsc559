package DatabaseManager;

import MainServer.ElectionCore.ElectionController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class DBMain {

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication.run(DatabaseController.class, args);

    }
}
