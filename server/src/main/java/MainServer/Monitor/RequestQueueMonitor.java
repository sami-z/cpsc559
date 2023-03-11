package MainServer.Monitor;

import MainServer.ServerState;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static Util.NetworkConstants.REQUEST_QUEUE_IPS;
import static Util.NetworkConstants.SERVER_IPS;

public class RequestQueueMonitor implements Runnable{
    public ObjectMapper mapper = new ObjectMapper();
    @Override
    public void run() {
        int i = 0;
        while (ServerState.serverIP.equals(ServerState.leaderIP)) {
            String ping_uri = NetworkConstants.getRequestQueueURIPing(REQUEST_QUEUE_IPS[i]);
            RestTemplate restTemplate = new RestTemplate();

            try {
                restTemplate.getForEntity(ping_uri, String.class);
            } catch (RestClientResponseException e) {
                i = (i + 1) % REQUEST_QUEUE_IPS.length;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                JsonNode rqNode = mapper.createObjectNode();
                ((ObjectNode)rqNode).put("leaderIP", REQUEST_QUEUE_IPS[i]);
                HttpEntity<String> rqUpdate =
                        new HttpEntity<String>(rqNode.toString(), headers);

                for (String requestQueueIP: REQUEST_QUEUE_IPS) {
                    String request_queue_uri = NetworkConstants.getResponseQueueURI(requestQueueIP);
                    restTemplate.postForEntity(request_queue_uri, rqUpdate, String.class);
                }

                JsonNode psNode = mapper.createObjectNode();
                ((ObjectNode)psNode).put("requestQueueIP", REQUEST_QUEUE_IPS[i]);
                HttpEntity<String> psUpdate =
                        new HttpEntity<String>(psNode.toString(), headers);

                for (String serverIP: SERVER_IPS) {
                    String processing_server_uri = NetworkConstants.getProcessingServerURILeader(serverIP);
                    restTemplate.postForEntity(processing_server_uri, psUpdate, String.class);
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
