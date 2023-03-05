package MainServer.ExecutionCore;

import MainServer.ElectionCore.ElectionController;
import org.springframework.boot.SpringApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerMain {

    public static void main(String[] args) throws UnknownHostException {
        InetAddress IP=InetAddress.getLocalHost();
        SpringApplication.run(ElectionController.class,args);


    }
}
