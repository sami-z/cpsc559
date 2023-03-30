package MainServer.ExecutionCore;

import MainServer.ElectionCore.ElectionConsumer;
import MainServer.ElectionCore.ElectionController;
import MainServer.Monitor.LeaderMonitor;
import MainServer.ServerState;
import Util.NetworkConstants;
import org.springframework.boot.SpringApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        String IP = in.readLine(); //you get the IP as a String

        ServerState.serverIP = IP;
        ServerState.leaderIP = IP;
        ServerState.requestQueueIP = "localhost";
        SpringApplication app = new SpringApplication(ElectionController.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", Integer.toString(NetworkConstants.MAIN_SERVER_PORT)));
        app.run(args);

        System.out.println("MAIN SERVER IS RUNNING");

        //new Thread(new LeaderMonitor()).start();
        new Thread(new MultiThreadedServer()).start();

    }
}
