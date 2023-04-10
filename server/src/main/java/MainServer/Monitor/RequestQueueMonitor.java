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

import static Util.NetworkConstants.REQUEST_QUEUE_IPS;
import static Util.NetworkConstants.SERVER_IPS;

public class RequestQueueMonitor implements Runnable{
    public static ObjectMapper mapper = new ObjectMapper();

    static RestTemplate restTemplate;

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
    }

    /**

     Sends an HTTP POST request to update the leader of the request queue and inform processing servers of the
     change in the leader.
     @param IP the IP address of the new leader of the request queue
     */
    public static void sendRequestQueueLeader(String IP){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode rqNode = mapper.createObjectNode();
        ((ObjectNode)rqNode).put("leaderIP", IP);
        HttpEntity<String> rqUpdate =
                new HttpEntity<String>(rqNode.toString(), headers);

        for (String requestQueueIP: REQUEST_QUEUE_IPS) {
            String request_queue_uri = NetworkConstants.getRequestQueueLeaderURI(requestQueueIP);
            try {
                restTemplate.postForEntity(request_queue_uri, rqUpdate, String.class);
            } catch(RestClientException e){
                System.out.println("Could not update leader for " + request_queue_uri);
            }
        }

        JsonNode psNode = mapper.createObjectNode();
        ((ObjectNode)psNode).put("requestQueueIP", IP);
        HttpEntity<String> psUpdate =
                new HttpEntity<String>(psNode.toString(), headers);

                for (String serverIP: SERVER_IPS) {
                    String processing_server_uri = NetworkConstants.getProcessingServerURILeader(serverIP);

                    try {
                        restTemplate.postForEntity(processing_server_uri, psUpdate, String.class);
                    } catch (RestClientException e){}
                }
    }

    /**
     This method sends a ping request to the request queue IPs to get the currently running request queue.
     It iterates through all the request queue IPs and sends a ping request to each of them. If a response is received from
     any of them, it returns that IP address, which is the currently running request queue. If none of the request queues
     responds to the ping request, the method returns null.
     @return The IP address of the currently running request queue or null if none of the request queues respond.
     */
    public static String getRunningRequestQueue(){
        for(String requestIP : REQUEST_QUEUE_IPS){
            String ping_uri = NetworkConstants.getRequestQueueURIPing(requestIP);
            try {
                restTemplate.getForEntity(ping_uri, String.class);
                return requestIP;
            } catch(RestClientException e){
                System.out.println("Request queue did not respond to ping: " + requestIP);
            }
        }

        return null;
    }

    /**
     * This code is used to check if the request queue leader is down.
     * If the request queue leader is down we elect a new leader and send this
     * information to all the other request queues and the processing servers
     * */
    @Override
    public void run() {

        if(ServerState.requestQueueIP.isEmpty()){
            String newLeader = getRunningRequestQueue();
            sendRequestQueueLeader(newLeader);
        }

        while(ServerState.serverIP.equals(ServerState.leaderIP)){
            String ping_uri = NetworkConstants.getRequestQueueURIPing(ServerState.requestQueueIP);
            try{
                restTemplate.getForEntity(ping_uri,String.class);
            }catch (RestClientException e){
                String newLeader = getRunningRequestQueue();
                sendRequestQueueLeader(newLeader);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
