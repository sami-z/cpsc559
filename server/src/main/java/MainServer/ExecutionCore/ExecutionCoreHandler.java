package MainServer.ExecutionCore;

import DatabaseManager.ReplicationRunner;
import MainServer.ServerState;
import Util.DB;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.*;
import java.util.ArrayList;
import Util.NetworkUtil;
import static com.mongodb.client.model.Filters.eq;

public class ExecutionCoreHandler {
    public static void obtainLock(String IP, JsonNode request) {
        System.out.println("trying to obtain lock");
        int headOrder = -1;
        int currOrder = request.get("orderValue").asInt();
        while (headOrder != currOrder) {
            headOrder = NetworkUtil.getRequestHead(IP, request.get("fileName").asText());
            System.out.println("headOrder: " + headOrder);
            System.out.println("Current order: " + currOrder);
        }

    }

    public static void updateShare(JsonNode request) throws IOException {
        DB db = new DB();
        JsonNode file = db.loadFile(request.get("userName").asText(), request.get("fileName").asText());
        ((ObjectNode)file).put("responseType", "SINGLE");

        String usernames = request.get("shareWith").asText();
        String[] users = usernames.split(",");
        for (String username : users) {
            ((ObjectNode)file).put("userName", username);
            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(file, IP);
            }
        }
    }

    public static void processEvent(JsonNode request) throws IOException {
        DB db = new DB();
        // Parse HTML
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (request == null) return;

        String requestType = request.get("requestType").asText();

        if(requestType.equalsIgnoreCase("READ_ALL_FILES")){
            System.out.println("database requestType" + System.currentTimeMillis());
            ArrayList<JsonNode> files = db.findFiles(request.get("userName").asText());

            JsonNode response;

            ObjectMapper objectMapper = new ObjectMapper();
            if (!files.isEmpty()){
                response = objectMapper.valueToTree(files);
            }
            else{
                response = objectMapper.createObjectNode();
                ((ObjectNode)response).put("responseType", "ALLFILESEMPTY");
                ((ObjectNode)response).put("userName", request.get("userName").asText());
            }

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
//                    for (JsonNode file : files) {
//                        System.out.println("the actual file"+file);
//                        sendToResponseQueue(file, IP);
//                    }
                NetworkUtil.sendToResponseQueue(response, IP);

//                    sendToResponseQueue(response, IP);
            }
            System.out.println("database blah" + System.currentTimeMillis());
        }else if(requestType.equalsIgnoreCase("READ")){
            System.out.println("DATABASE SINGLE BEFORE" + System.currentTimeMillis());
            JsonNode singleFile = db.loadFile(request.get("userName").asText(), request.get("fileName").asText());
            System.out.println("DATABASE SINGLE AFTER LOAD" + System.currentTimeMillis());

            ((ObjectNode)singleFile).put("responseType", "SINGLE");
            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
//                    for (JsonNode file : files) {
//                        System.out.println("the actual file"+file);
//                        sendToResponseQueue(file, IP);
//                    }
                NetworkUtil.sendToResponseQueue(singleFile, IP);
            }
            System.out.println("DATABASE SINGLE FOR LOOP" + System.currentTimeMillis());
        } else if(requestType.equalsIgnoreCase("LOGIN")){
            FindIterable<Document> entry = db.getLoginReplica(true).find(eq("userName", request.get("userName").asText()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("userName", request.get("userName").asText());
            ((ObjectNode)response).put("responseType", "LOGIN");

            for (Document doc : entry) {
                String actualUserName = doc.getString("userName");
                String actualPassword = doc.getString("password");

                if (request.get("userName").asText().equals(actualUserName) && request.get("password").asText().equals(actualPassword)) {
                    ((ObjectNode) response).put("loggedIn", "SUCCESS");
                    break; // break the loop once a match is found
                } else {
                    ((ObjectNode) response).put("loggedIn", "FAILURE");
                }
            }

            if (!response.has("loggedIn")) {
                // handle case where no match was found
                ((ObjectNode) response).put("loggedIn", "FAILURE");
            }

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
        } else if(requestType.equalsIgnoreCase("WRITE")){
            String fileName = request.get("fileName").asText();
            obtainLock(ServerState.requestQueueIP,request);

            System.out.println("Send to database" + System.currentTimeMillis());

            System.out.println("Request is: " + request);


            boolean wasReplaced = NetworkUtil.sendWrite(request);

            if (!wasReplaced) {
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(request, IP);
                }
                updateShare(request);
            }

            System.out.println("database write done" + System.currentTimeMillis());

            System.out.println("Responsequeue sent" + System.currentTimeMillis());

            NetworkUtil.releaseLock(ServerState.requestQueueIP,fileName);

        } else if(requestType.equalsIgnoreCase("SHARE")){
            System.out.println("SHARING WITH: " + request.get("shareWith").toString());
            NetworkUtil.sendShare(request);
            updateShare(request);
        } else if(requestType.equalsIgnoreCase("DELETE")){

            String deleteList = NetworkUtil.sendDelete(request);
            System.out.println("PRINITNG  DELETE LIST: " + deleteList);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("userName", request.get("userName").asText());
            ((ObjectNode)response).put("responseType", "DELETE");
            ((ObjectNode)response).put("delete", deleteList);



            System.out.println("PRINITNG RESPONSE LIST: " + response);


            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
        } else if (requestType.equals("REGISTER")){
            boolean wasSuccessful = NetworkUtil.sendRegister(request);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("userName", request.get("userName").asText());
            ((ObjectNode)response).put("responseType", "REGISTER");

            if (wasSuccessful) {
                ((ObjectNode) response).put("registered", "SUCCESS");
            } else {
                ((ObjectNode) response).put("registered", "FAILURE");
            }

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
        }
        else{
            System.out.println("invalid request type");
            return;
        }

    }


}
