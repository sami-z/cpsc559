package MainServer.ExecutionCore;

import MainServer.Models.ClientRequestModel;
import Util.DB;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Objects;


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
        String uri = NetworkConstants.getDBManagerURI();

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        restTemplate.postForEntity(uri,request,String.class);
    }

    public static void sendToResponseQueue(JsonNode rq, String IP){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getResponseQueueURI(IP);

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


        // Check type of request
        if(request.get("requestType").asText().equalsIgnoreCase("READ")){ // locking
            String readType = request.get("readType").asText();
            DB db = new DB();
            ArrayList<JsonNode> files = db.findFiles(request.get("userName").asText());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode response = objectMapper.valueToTree(files);
            System.out.println("trying to print first element" + response.get(0));
            if(readType.equals("allFiles")){
                for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
//                    for (JsonNode file : files) {
//                        System.out.println("the actual file"+file);
//                        sendToResponseQueue(file, IP);
//                    }
                    sendToResponseQueue(response, IP);
                }
            }

        }
        else if(request.get("requestType").asText().equalsIgnoreCase("WRITE")){

            // TODO obtain lock

            sendWrite(request);
            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                sendToResponseQueue(request, IP);
            }
            // TODO release lock
        }
        else{
            System.out.println("invalid request type (must be READ or WRITE)");
            return;
        }
    }
}
