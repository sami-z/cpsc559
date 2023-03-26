package MainServer.Monitor;

import MainServer.ElectionCore.ElectionConsumer;
import MainServer.ServerState;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.time.Duration;

import static Util.NetworkConstants.SERVER_IPS;

public class LeaderMonitor implements Runnable{

    public static ObjectMapper mapper = new ObjectMapper();

    static RestTemplate restTemplate;

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
    }

    @Override
    public void run() {

        try {
            while (true) {
                if (ServerState.leaderIP.isEmpty()) {
                    System.out.println("The current leaderIP is NULL");
                    ElectionConsumer.initiateElection();
                    Thread.sleep(10000);
                    continue;
                }

                String ping_uri = NetworkConstants.getProcessingServerURIPing(ServerState.leaderIP);
                System.out.println(ping_uri);
                try {
                    restTemplate.getForEntity(ping_uri, String.class);
                } catch (RestClientException e){
                    System.out.println("Leader did not respond to ping");
                    ElectionConsumer.initiateElection();
                    Thread.sleep(10000);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e){}
    }
}
