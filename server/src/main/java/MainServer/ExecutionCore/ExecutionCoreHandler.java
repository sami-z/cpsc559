package MainServer.ExecutionCore;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.Gson;
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
            headOrder = NetworkUtil.getRequestHead(IP, request.get("keyValue").asText(),currOrder);
            System.out.println("headOrder: " + headOrder);
            System.out.println("Current order: " + currOrder);
        }
        ServerState.requestQueueDown = false;

    }

    public static void updateDelete(JsonNode request) throws IOException {
        JsonNode files = request.get("filesToDelete");
        for (JsonNode file: files) {
            JsonNode response = new ObjectMapper().createObjectNode();
            ((ObjectNode) response).put("responseType", "DELETE");
            ArrayList<String> shared = new ObjectMapper().convertValue(file.get("shared"), ArrayList.class);
            for (String username : shared) {
                ((ObjectNode) response).put("userName", username);
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(response, IP);
                }
            }
        }
    }
    public static void updateShare(JsonNode request, Boolean updateShare) throws IOException {
        DB db = new DB();
        if (updateShare) { //file(s) shared with new user(s)
            ArrayList<String> filesToShare = new ObjectMapper().convertValue(request.get("filesToShare"), ArrayList.class);
            for (String fileName : filesToShare) {
                System.out.println("inupdateSHARE " + fileName + " " + request.get("userName").asText());
                JsonNode file = db.loadFile(request.get("userName").asText(), fileName);
                ((ObjectNode) file).put("responseType", "UPDATE");

                ArrayList<String> users = new ObjectMapper().convertValue(request.get("shared"), ArrayList.class);
                users.add(request.get("userName").asText());
                for (String username : users) {
                    ((ObjectNode) file).put("userName", username);
                    for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                        NetworkUtil.sendToResponseQueue(file, IP);
                    }
                }
            }
        }
        else { //existing file replaced
            String fileName = request.get("fileName").asText();
            JsonNode file = db.loadFile(request.get("userName").asText(), fileName);
            ((ObjectNode) file).put("responseType", "UPDATE");
            ArrayList<String> users = new ObjectMapper().convertValue(request.get("shared"), ArrayList.class);
            users.add(request.get("userName").asText()); //requesting client also gets response to update file
            for (String username : users) {
                ((ObjectNode) file).put("userName", username);
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(file, IP);
                }
            }
        }
        db.closeMongoClients();
    }

    public static void processEvent(JsonNode request) throws IOException {
        // Parse HTML
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (request == null) return;

        String requestType = request.get("requestType").asText();

        if(requestType.equalsIgnoreCase("READ_ALL_FILES")){
            DB db = new DB();
            System.out.println("database requestType" + System.currentTimeMillis());

            ArrayNode files = db.findFiles(request.get("userName").asText());

            JsonNode response;

            ObjectMapper objectMapper = new ObjectMapper();
            if (!files.isEmpty()){
                //response = objectMapper.valueToTree(files);
                response = objectMapper.createObjectNode();
                ((ObjectNode)response).put("userName", request.get("userName").asText());
                ((ObjectNode)response).put("responseType", "LOADALLFILES");
                ((ObjectNode)response).set("files", files);
            }
            else{
                response = objectMapper.createObjectNode();
                ((ObjectNode)response).put("responseType", "ALLFILESEMPTY");
                ((ObjectNode)response).put("userName", request.get("userName").asText());
            }

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
            System.out.println("database blah" + System.currentTimeMillis());
        }else if(requestType.equalsIgnoreCase("DOWNLOAD")){
            System.out.println("DATABASE SINGLE BEFORE" + System.currentTimeMillis());

            DB db = new DB();
            JsonNode singleFile = db.loadFile(request.get("userName").asText(), request.get("fileName").asText());

            System.out.println("DATABASE SINGLE AFTER LOAD" + System.currentTimeMillis());

            ((ObjectNode)singleFile).put("responseType", "DOWNLOAD");
            ((ObjectNode)singleFile).put("userName", request.get("userName").asText()); //overwrite with client's userName
            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(singleFile, IP);
            }
            System.out.println("DATABASE SINGLE FOR LOOP" + System.currentTimeMillis());
            db.closeMongoClients();
        } else if(requestType.equalsIgnoreCase("LOGIN")){

            DB db = new DB();
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
            db.closeMongoClients();
        } else if(requestType.equalsIgnoreCase("WRITE")){
            String keyValue = request.get("keyValue").asText();
            int currOrder = request.get("orderValue").asInt();
            obtainLock(ServerState.requestQueueIP,request);

            System.out.println("Send to database" + System.currentTimeMillis());

            System.out.println("Request is: " + request);


            boolean wasReplaced = NetworkUtil.sendWrite(request);

            System.out.println("database write done" + System.currentTimeMillis());

            if (wasReplaced) {
                updateShare(request,false);
            }
            else{
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(request, IP);
                }
            }



            System.out.println("Responsequeue sent" + System.currentTimeMillis());

            NetworkUtil.releaseLock(ServerState.requestQueueIP,keyValue,currOrder);

        } else if(requestType.equalsIgnoreCase("SHARE")){
            System.out.println("SHARING WITH: " + request.get("shared").toString());
            NetworkUtil.sendShare(request);
            updateShare(request, true);
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
            updateDelete(request);
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
