package MainServer.ExecutionCore;

import DatabaseManager.ReplicationRunner;
import MainServer.ServerState;
import Util.DB;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.catalina.Server;

import java.io.*;
import java.util.ArrayList;

import static Util.NetworkUtil.*;


public class ExecutionCoreHandler {

    private ObjectMapper mapper = new ObjectMapper();

    public static void processEvent(JsonNode request) throws IOException {
        DB db = new DB();
        // Parse HTML
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (request == null) return;


        // Check type of request
        if(request.get("requestType").asText().equalsIgnoreCase("READ")){ // locking
            System.out.println("database requestType" + System.currentTimeMillis());
            String readType = request.get("readType").asText();

//            ArrayList<JsonNode> files = db.findFiles(request.get("userName").asText());
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode response = objectMapper.valueToTree(files);
//            System.out.println("trying to print first element" + response.get(0));
            System.out.println("database requestType" + System.currentTimeMillis());
            if(readType.equals("allFiles")){
                ArrayList<JsonNode> files = db.findFiles(request.get("userName").asText());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode response = objectMapper.valueToTree(files);
                for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
//                    for (JsonNode file : files) {
//                        System.out.println("the actual file"+file);
//                        sendToResponseQueue(file, IP);
//                    }
                    sendToResponseQueue(response, IP);
                }
                System.out.println("database blah" + System.currentTimeMillis());
            }

            else if(readType.equals("SINGLE")){
                System.out.println("DATABASE SINGLE BEFORE" + System.currentTimeMillis());
                JsonNode singleFile = db.loadFile(request.get("fileName").asText());
                System.out.println("DATABASE SINGLE AFTER LOAD" + System.currentTimeMillis());

                ((ObjectNode)singleFile).put("readType", "SINGLE");
                for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
//                    for (JsonNode file : files) {
//                        System.out.println("the actual file"+file);
//                        sendToResponseQueue(file, IP);
//                    }
                    sendToResponseQueue(singleFile, IP);
                }
                System.out.println("DATABASE SINGLE FOR LOOP" + System.currentTimeMillis());

            }

        }
        else if(request.get("requestType").asText().equalsIgnoreCase("WRITE")){
            String writeType = request.get("writeType").asText();
            // TODO obtain lock
            String fileName = request.get("fileName").asText();
            obtainLock(ServerState.requestQueueIP,fileName);

            if(writeType.equals("DELETE")){

                ArrayList<String> arr = new ObjectMapper().convertValue(request.get("filesToDelete"), ArrayList.class);

                ArrayList<String> arr2 = db.deleteFile(arr, false);
                new Thread(new ReplicationRunner(null, arr)).start();
                System.out.println("PRINITNG  DELETE LIST: " + arr2);

                String listString = String.join(",", arr2);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode response = mapper.createObjectNode();
                ((ObjectNode)response).put("userName", request.get("userName").asText());
                ((ObjectNode)response).put("readType", "DELETE");
                ((ObjectNode)response).put("delete", listString);



                System.out.println("PRINITNG RESPONSE LIST: " + response);


                for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                    sendToResponseQueue(response, IP);
                }
            }

            else if (writeType.equals("SHARE")) {
                ArrayList<String> arr = new ObjectMapper().convertValue(request.get("sharedWith"), ArrayList.class);
                db.editSharedWith(fileName, arr);

            }
            else{

                System.out.println("Send to database" + System.currentTimeMillis());

                System.out.println("Request is: " + request);


                sendWrite(request);

                System.out.println("database write done" + System.currentTimeMillis());
                for(String IP : NetworkConstants.RESPONSE_QUEUE_IPS){
                    sendToResponseQueue(request, IP);
                }

                System.out.println("Responsequeue sent" + System.currentTimeMillis());


                // TODO release lock

            }

            releaseLock(ServerState.requestQueueIP,fileName);
        }
        else{
            System.out.println("invalid request type (must be READ or WRITE)");
            return;
        }

    }


}
