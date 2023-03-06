package MainServer.ExecutionCore;

import Util.DB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;


public class ExecutionCoreHandler {

    private ObjectMapper mapper = new ObjectMapper();

    public void obtainLock(String filename){
        return;
    }

    public void releaseLock(String filename){
        return;
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

            RestTemplate restTemplate = new RestTemplate();
            String uri = "http://localhost:8080/api/request";
            JsonNode request = restTemplate.getForObject(uri, JsonNode.class);

            // TODO release lock
        }


    }
}
