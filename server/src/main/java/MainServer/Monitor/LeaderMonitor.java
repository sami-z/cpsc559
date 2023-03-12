package MainServer.Monitor;

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

import java.time.Duration;

import static Util.NetworkConstants.SERVER_IPS;

public class LeaderMonitor implements Runnable{

    public static ObjectMapper mapper = new ObjectMapper();

    static RestTemplate restTemplate;

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
    }

    public static void sendProcessingServerLeader(String IP){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode node = mapper.createObjectNode();
        ((ObjectNode)node).put("leaderIP", IP);
        HttpEntity<String> update =
                new HttpEntity<String>(node.toString(), headers);

        for (String serverIP: SERVER_IPS) {
            String processing_server_uri = NetworkConstants.getProcessingServerURILeaderServer(serverIP);

            try {
                restTemplate.postForEntity(processing_server_uri, update, String.class);
            } catch (RestClientException e){}
        }
    }

    public static String getRunningProcessingServer(){
        for(String serverIP : SERVER_IPS){
            String ping_uri = NetworkConstants.getProcessingServerURIPing(serverIP);
            try {
                restTemplate.getForEntity(ping_uri, String.class);
                return serverIP;
            } catch(RestClientException e){}
        }

        return null;
    }

    @Override
    public void run() {

        if(ServerState.leaderIP.isEmpty()){
            String newLeader = getRunningProcessingServer();
            sendProcessingServerLeader(newLeader);
        }

        while(ServerState.serverIP.equals(ServerState.leaderIP)){
            String ping_uri = NetworkConstants.getProcessingServerURIPing(ServerState.leaderIP);
            try{
                restTemplate.getForEntity(ping_uri,String.class);
            }catch (RestClientException e){
                String newLeader = getRunningProcessingServer();
                sendProcessingServerLeader(newLeader);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
