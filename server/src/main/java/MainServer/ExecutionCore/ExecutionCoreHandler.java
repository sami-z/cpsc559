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
import java.util.Arrays;
import java.util.List;

import Util.NetworkUtil;
import static com.mongodb.client.model.Filters.eq;

public class ExecutionCoreHandler {
    private static DB db;

    public static void initDB(){
        db = new DB();
    }

    /**

     This method tries to obtain a lock for the given request by communicating with the process identified by the provided IP address.
     It keeps checking the head of the request queue until the head order matches the order of the given request.
     Once the lock is obtained, the flag indicating that the request queue is down is set to false.
     @param IP the IP address of the process to communicate with
     @param request the request for which to obtain a lock
     */
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

    /**

     This method updates the delete request for the specified files in the system.
     It sends a DELETE response to the response queue of each shared user for each file.
     @param request a JsonNode object containing the list of files to be deleted and their shared users
     @throws IOException if there is an error while creating or sending the DELETE response to the response queues
     */
    public static void updateDelete(JsonNode request) throws IOException {
        JsonNode files = request.get("filesToDelete");
        for (JsonNode file: files) {
            JsonNode response = new ObjectMapper().createObjectNode();
            ((ObjectNode) response).put("responseType", "DELETE");
            String sharedString = new ObjectMapper().convertValue(file.get("shared"), String.class);
            String[] shared = sharedString.split(",");
            for (String user : shared) {
                ((ObjectNode) response).put("currentUser", user);
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(response, IP);
                }
            }
        }
    }

    /**

     This method updates the shared files for all users affected by an unsharing operation.
     It first retrieves the list of files to unshare from the request JSON object and iterates over it.
     For each file, it retrieves the list of users the file was shared with and sends a delete request to
     the response queues of each of those users. The delete request contains the name of the file and the
     current user making the request. Finally, the method closes the MongoDB client connections.
     @param request The JSON object containing the request data.
     @throws IOException If there is an issue with converting the JSON object to the required format.
     */
    public static void updateUnshare(JsonNode request) throws IOException {
        ArrayList<String> filesToUnshare = new ObjectMapper().convertValue(request.get("filesToUnshare"), ArrayList.class);
        for (String fileName : filesToUnshare) {
            JsonNode response = new ObjectMapper().createObjectNode();
            ((ObjectNode) response).put("responseType", "DELETE");
            JsonNode file = db.loadFile(request.get("currentUser").asText(), fileName);
            String shared = new ObjectMapper().convertValue(file.get("shared"), String.class);
            List<String> bigShared = Arrays.asList(shared.split("\\s*,\\s*"));

            for (String user : bigShared) {
                ((ObjectNode) response).put("currentUser", user);
                ((ObjectNode)response).put("delete", fileName);
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(response, IP);
                }
            }
        }
    }

    /**

     Updates the share status of a file or multiple files.
     @param request A JSON object containing the details of the request including the files to be shared, the user(s) with whom they will be shared, and other relevant information.
     @param updateShare A boolean indicating whether the file(s) are being shared with new user(s) or an existing file is being replaced.
     @throws IOException If there is an error in the input or output.
     */
    public static void updateShare(JsonNode request, Boolean updateShare) throws IOException {
        if (updateShare) { //file(s) shared with new user(s)
            ArrayList<String> filesToShare = new ObjectMapper().convertValue(request.get("filesToShare"), ArrayList.class);
            for (String fileName : filesToShare) {
                System.out.println("inupdateSHARE " + fileName + " " + request.get("currentUser").asText());
                JsonNode file = db.loadFile(request.get("currentUser").asText(), fileName);
                ((ObjectNode) file).put("responseType", "UPDATE");

                ArrayList<String> users = new ObjectMapper().convertValue(request.get("shared"), ArrayList.class);

                users.add(request.get("currentUser").asText());
                for (String username : users) {
                    ((ObjectNode) file).put("currentUser", username);
                    for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                        NetworkUtil.sendToResponseQueue(file, IP);
                    }
                }
            }
        }
        else { //existing file replaced
            String fileName = request.get("fileName").asText();
            JsonNode file = db.loadFile(request.get("currentUser").asText(), fileName);
            ((ObjectNode) file).put("responseType", "UPDATE");
            ArrayList<String> users = new ObjectMapper().convertValue(request.get("shared"), ArrayList.class);
            users.add(request.get("currentUser").asText()); //requesting client also gets response to update file
            for (String username : users) {
                ((ObjectNode) file).put("currentUser", username);
                for (String IP : NetworkConstants.RESPONSE_QUEUE_IPS) {
                    NetworkUtil.sendToResponseQueue(file, IP);
                }
            }
        }
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

        /**

         This if block handles the READ_ALL_FILES request type by retrieving all files from the database
         belonging to the current user and sending the list of files to the response queue for all IPs
         to receive. If there are files in the database, the method creates a JSON response object containing
         the files, otherwise it creates a response object indicating that there are no files.
         @param request The JSON request object containing the current user's ID
         @throws IOException if there is an issue with sending the response object to the response queue
         */
        if(requestType.equalsIgnoreCase("READ_ALL_FILES")){
            System.out.println("database requestType" + System.currentTimeMillis());

            ArrayNode files = db.findFiles(request.get("currentUser").asText());

            JsonNode response;

            ObjectMapper objectMapper = new ObjectMapper();
            if (!files.isEmpty()){
                //response = objectMapper.valueToTree(files);
                response = objectMapper.createObjectNode();
                ((ObjectNode)response).put("currentUser", request.get("currentUser").asText());
                ((ObjectNode)response).put("responseType", "LOADALLFILES");
                ((ObjectNode)response).set("files", files);
            }
            else{
                response = objectMapper.createObjectNode();
                ((ObjectNode)response).put("responseType", "ALLFILESEMPTY");
                ((ObjectNode)response).put("currentUser", request.get("currentUser").asText());
            }

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
            System.out.println("database blah" + System.currentTimeMillis());
        }

        /**

         This if block loads a single file from the database and sends it to the response queue for download.
         @param request the JsonNode containing the necessary information to load the requested file.

         @throws IOException if there is an error while loading or sending the file.
         */
        else if(requestType.equalsIgnoreCase("DOWNLOAD")){
            System.out.println("DATABASE SINGLE BEFORE" + System.currentTimeMillis());

            JsonNode singleFile = db.loadFile(request.get("ownerName").asText(), request.get("fileName").asText());

            System.out.println("DATABASE SINGLE AFTER LOAD" + System.currentTimeMillis());

            ((ObjectNode)singleFile).put("responseType", "DOWNLOAD");
            ((ObjectNode)singleFile).put("currentUser", request.get("currentUser").asText()); //overwrite with client's userName
            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(singleFile, IP);
            }
            System.out.println("DATABASE SINGLE FOR LOOP" + System.currentTimeMillis());
        }

        /**

         This if block handles the login request from the client by verifying the provided username and password
         against the database. It then sends a response to the response queue with the result of the login attempt.
         @param request the JsonNode representing the login request from the client
         @throws IOException if an I/O error occurs while sending the response to the response queue
         */
        else if(requestType.equalsIgnoreCase("LOGIN")){

            FindIterable<Document> entry = db.getLoginReplica(true).find(eq("userName", request.get("currentUser").asText()));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("currentUser", request.get("currentUser").asText());
            ((ObjectNode)response).put("responseType", "LOGIN");

            for (Document doc : entry) {
                String actualUserName = doc.getString("userName");
                String actualPassword = doc.getString("password");

                if (request.get("currentUser").asText().equals(actualUserName) && request.get("password").asText().equals(actualPassword)) {
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
        }

        /**

         This if block handles the WRITE request sent by the client. It first obtains the lock on the key-value pair
         by calling the obtainLock method. Then it sends the request to the database using the NetworkUtil.sendWrite method.
         After that, it checks whether the request was successful in replacing an existing entry or not. If it was, it calls
         the updateShare method to update the replica servers. Otherwise, it sends the request to the response queue for all
         the replica servers using the NetworkUtil.sendToResponseQueue method. Finally, it releases the lock by calling
         the NetworkUtil.releaseLock method.
         @param request The request sent by the client.
         */
        else if(requestType.equalsIgnoreCase("WRITE")){
            String keyValue = request.get("keyValue").asText();
            int currOrder = request.get("orderValue").asInt();
            obtainLock(ServerState.requestQueueIP,request);

            System.out.println("Send to database" + System.currentTimeMillis());

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

        }

        /**

         This if block is called when a client sends a "SHARE" request to the server. The server will send the request to the other nodes in the cluster, so they can update their shared state.
         @param request The request made by the client containing information about the share.
         */
        else if(requestType.equalsIgnoreCase("SHARE")){
            System.out.println("SHARING WITH: " + request.get("shared").toString());
            NetworkUtil.sendShare(request);
            updateShare(request, true);
        }

        /**

         This if block is called when a request of type "UNSHARE" is received.
         It updates the database to remove the user(s) with whom a file was shared, and sends a message to all other servers
         to do the same.
         @param request the JSON request received from the client
         */
        else if(requestType.equalsIgnoreCase("UNSHARE")){
            System.out.println("UNSHARING WITH: " + request.get("unshared").toString());
            updateUnshare(request);
            NetworkUtil.sendUnShare(request);
        }

        /**

         This if block is called when the received request type is "DELETE". It sends a request to delete the specified file(s) to the server and receives a list of deleted files.
         It creates a JsonNode object that represents the response message to be sent to the response queue. The object includes the list of deleted files, and the user who sent the request.
         It then sends the response to the response queue for all response queue IPs. Finally, It calls the "updateDelete" method to update the local shared file list.
         @param request a JsonNode object that represents the received request
         */
        else if(requestType.equalsIgnoreCase("DELETE")){

            String deleteList = NetworkUtil.sendDelete(request);
            System.out.println("PRINITNG  DELETE LIST: " + deleteList);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("currentUser", request.get("currentUser").asText());
            ((ObjectNode)response).put("responseType", "DELETE");
            ((ObjectNode)response).put("delete", deleteList);

            System.out.println("PRINITNG RESPONSE LIST: " + response);

            for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                NetworkUtil.sendToResponseQueue(response, IP);
            }
            updateDelete(request);
        }

        /**

         Handles the case where the request type is "REGISTER".
         Sends the request to other servers to register the user.
         Creates a response indicating whether the registration was successful or not and sends it to the response queue.
         @param request the request JSON object containing information about the user to be registered
         */
        else if (requestType.equals("REGISTER")){
            boolean wasSuccessful = NetworkUtil.sendRegister(request);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.createObjectNode();
            ((ObjectNode)response).put("currentUser", request.get("currentUser").asText());
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
