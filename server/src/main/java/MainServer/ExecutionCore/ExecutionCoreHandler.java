package MainServer.ExecutionCore;

import MainServer.Models.ClientRequestModel;
import Util.DB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;


public class ExecutionCoreHandler {

    private ObjectMapper mapper = new ObjectMapper();

    public static void obtainLock(String filename){
        return;
    }

    public static void releaseLock(String filename){
        return;
    }

    public static void sendWrite(JsonNode rq){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = "http://localhost:8080/dbmanager/upload";

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        restTemplate.postForEntity(uri,request,String.class);
    }

    public static void sendToResponseQueue(JsonNode rq){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = "http://localhost:8080/api/response";

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        restTemplate.postForEntity(uri,request,String.class);
    }

    public static void processEvent(JsonNode request) throws IOException {
        // Parse HTML

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (request == null) return;

        ArrayList<JsonNode> files = null;
        // Check type of request
        if(request.get("requestType").asText().equalsIgnoreCase("READ")){ // locking
            DB db = new DB();
            files = db.findFiles(request.get("userName").asText());

        }
        else if(request.get("requestType").asText().equalsIgnoreCase("WRITE")){

            // TODO obtain lock

            sendWrite(request);

            // TODO release lock

            sendToResponseQueue(request);
        }


    }
}
