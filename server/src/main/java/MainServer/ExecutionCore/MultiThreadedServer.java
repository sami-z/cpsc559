package MainServer.ExecutionCore;

import MainServer.ServerState;
import Util.DB;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiThreadedServer implements Runnable{
    ServerSocket serverSocket = null;
    boolean isRunning = false;

    public synchronized void stop() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;

        while(this.isRunning){
            RestTemplate restTemplate = new RestTemplate();
            String fetchRequestURI = NetworkConstants.getRequestQueueURI(ServerState.requestQueueIP);
            JsonNode request = null;
            try {
                request = restTemplate.getForObject(fetchRequestURI, JsonNode.class);
            } catch (RestClientException e){}
            // change this to correct check to see if nothing was in queue
            if (request == null) continue;

            try {
                System.out.println("server got request" + System.currentTimeMillis());
                System.out.println(request.toPrettyString());
                ExecutionCoreHandler.processEvent(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Server Stopped.") ;
        System.out.println("Closing server");
    }
}