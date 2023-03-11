package MainServer.ExecutionCore;

import DatabaseManager.DatabaseController;
import MainServer.ElectionCore.ElectionConsumer;
import MainServer.ElectionCore.ElectionController;
import MainServer.ServerState;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        String IP = in.readLine(); //you get the IP as a String

        ServerState.serverIP = IP;


        SpringApplication app = new SpringApplication(ElectionController.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.MAIN_SERVER_PORT)));
        app.run(args);

        ElectionConsumer.initiatieElection();

        System.out.println("MAIN SERVER IS RUNNING");
        new Thread(new MultiThreadedServer()).start();


    }
}
