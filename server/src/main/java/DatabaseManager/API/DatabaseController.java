package DatabaseManager.API;

import DatabaseManager.DBMain;
import DatabaseManager.DBManagerState;
import DatabaseManager.DatabaseClusterMonitor;
import DatabaseManager.ReplicationRunner;
import DatabaseManager.Service.DatabaseHandler;
import MainServer.Models.ClientRequestModel;
import RequestQueue.Leader.LeaderState;
import Util.DB;
import Util.DBConstants;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

@RestController
@RequestMapping("/api/dbmanager")
public class DatabaseController {
    private final DatabaseHandler databaseHandler;

    @Autowired
    public DatabaseController(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
        DBManagerState.DBLeaderIP = "";
    }

    /**

     This method is responsible for handling the HTTP POST request with a path of "/upload".

     It takes in a JSON request body in the form of a ClientRequestModel object and processes it.

     @param requestModel a ClientRequestModel object that contains the details of the uploaded file such as the

     @return a String object indicating the success or failure of the upload operation.

     @throws Exception if there is an error while processing the request.
     */
    @PostMapping("/upload")
    public String uploadToDatabase(@RequestBody ClientRequestModel requestModel) {
        System.out.println("0");
        DB db = new DB();

        System.out.println("hello i am here: " + requestModel.fileName);
        Document query = db.createUploadQuery(requestModel.currentUser, requestModel.fileName);

        Document queryResult = db.getReplica(true).find(query).first();
        String ownerName;
        System.out.println("2");

        if (queryResult == null) {
            ownerName = requestModel.currentUser;
        } else {
            ownerName = queryResult.getString("userName");
        }

        System.out.println("4");
        long timestamp = System.currentTimeMillis();
        databaseHandler.updateTimestamp(ownerName, requestModel.fileName, timestamp);
        ArrayList<Document> docs = db.uploadFile(requestModel, timestamp, queryResult);
        db.closeMongoClients();
        System.out.println("5");
        new Thread(new ReplicationRunner(docs.get(0), null, null, null, null, null,0,null, true, false, false, false)).start();
        if (docs.get(1) == null) {
            return Boolean.toString(false);
        } else {
            return Boolean.toString(true);
        }
    }

    /**

     This method is responsible for handling the HTTP POST request with a path of "/delete".

     It takes in a JSON request body containing the list of files to be deleted and the name of the user who

     initiated the delete operation.

     @param deleteRequest a JsonNode object representing the JSON request body containing the details of the files

     @return a String object indicating the success or failure of the delete operation.

     @throws Exception if there is an error while processing the request.
     */
    @PostMapping("/delete")
    public String deleteFromDatabase(@RequestBody JsonNode deleteRequest) {
        DB db = new DB();
        ArrayList<String> filesToDelete = new ArrayList<>();
        for (final JsonNode file : deleteRequest.get("filesToDelete")) {
            filesToDelete.add(file.get("fileName").asText());
        }

        String userName = deleteRequest.get("currentUser").asText();
        ArrayList<ArrayList<String>> tsList = new ArrayList<>();
        for (String fileName : filesToDelete) {
            long timestamp = System.currentTimeMillis();
            databaseHandler.updateTimestamp(userName, fileName, timestamp);

            ArrayList<String> innerTSList = new ArrayList<>();
            innerTSList.add(fileName);
            innerTSList.add(Long.toString(timestamp));
            tsList.add(innerTSList);
        }
        String deletedFiles = db.deleteFile(filesToDelete, userName);
        new Thread(new ReplicationRunner(null, null, null, null, null,  userName,0, tsList, false, false, false, false)).start();
        db.closeMongoClients();
        return deletedFiles;
    }

    /**

     This method is responsible for handling the HTTP POST request with a path of "/share". It takes in a JSON request
     body containing the list of files to be shared, the names of the users to share the files with, and the name of the
     user who initiated the sharing operation.
     @param shareRequest a JsonNode object representing the JSON request body containing the details of the files

     @throws Exception if there is an error while processing the request.
     */
    @PostMapping("/share")
    public void editShare(@RequestBody JsonNode shareRequest) {
        DB db = new DB();
        String userName = shareRequest.get("currentUser").asText();
        ArrayList<String> shareList = new ObjectMapper().convertValue(shareRequest.get("shared"), ArrayList.class);
        ArrayList<String> filesToShare = new ObjectMapper().convertValue(shareRequest.get("filesToShare"), ArrayList.class);
        ArrayList<ArrayList<String>> tsList = new ArrayList<>();
        for (String fileName:filesToShare){
            long timestamp = System.currentTimeMillis();
            databaseHandler.updateTimestamp(userName, fileName, timestamp);
            ArrayList<String> innerTSList = new ArrayList<>();
            innerTSList.add(fileName);
            innerTSList.add(Long.toString(timestamp));
            tsList.add(innerTSList);
        }
        db.editSharedWith(filesToShare, userName, shareList);
        new Thread(new ReplicationRunner(null, shareList, filesToShare, null, null, userName,0, tsList, false, false, true, false)).start();
        db.closeMongoClients();
    }

    /**

     This method is responsible for handling the HTTP POST request with a path of "/unshare". It takes in a JSON request
     body containing the list of files to be unshared, the names of the users to unshare the files with, and the name of
     the user who initiated the unsharing operation.
     @param unshareRequest a JsonNode object representing the JSON request body containing the details of the files

     @throws Exception if there is an error while processing the request.
     */
    @PostMapping("/unshare")
    public void editUnshare(@RequestBody JsonNode unshareRequest) {
        DB db = new DB();
        String userName = unshareRequest.get("currentUser").asText();
        ArrayList<String> unshareList = new ObjectMapper().convertValue(unshareRequest.get("unshared"), ArrayList.class);
        ArrayList<String> filesToUnShare = new ObjectMapper().convertValue(unshareRequest.get("filesToUnshare"), ArrayList.class);
        ArrayList<ArrayList<String>> tsList = new ArrayList<>();
        for (String fileName:filesToUnShare){
            long timestamp = System.currentTimeMillis();
            databaseHandler.updateTimestamp(userName, fileName, timestamp);
            ArrayList<String> innerTSList = new ArrayList<>();
            innerTSList.add(fileName);
            innerTSList.add(Long.toString(timestamp));
            tsList.add(innerTSList);
        }
        db.editUnsharedWith(filesToUnShare, userName, unshareList);
        new Thread(new ReplicationRunner(null, null, null, unshareList, filesToUnShare, userName,0, tsList, false, false, false, true)).start();
        db.closeMongoClients();
    }

    /**

     Retrieves the timestamp of the given file key from the database.
     @param key the key representing the file in the database
     @return the timestamp of the file as a string
     */
    @GetMapping("/get-head/{key}")
    @ResponseBody
    public String getTimestamp(@PathVariable String key) {
        return Long.toString(databaseHandler.getTimestamp(key));
    }

    /**

     Registers a new user in the system and adds their details to the database.
     @param requestModel A ClientRequestModel object representing the details of the new user to be registered
     @return A string representation of a boolean value indicating whether the registration was successful or not.
     */
   @PostMapping("/register")
   @ResponseBody
   public String registerUser(@RequestBody ClientRequestModel requestModel) {
        DB db = new DB();
        Document replicatedEntry = db.registerUser(requestModel);
        if (replicatedEntry == null) {
            db.closeMongoClients();
            return Boolean.toString(false);
        }
        new Thread(new ReplicationRunner(replicatedEntry, null, null, null, null, null,0, null,false, true, false, false)).start();
        db.closeMongoClients();
        return Boolean.toString(true);
    }

    @GetMapping("/ping")
    public void ping(){
        return;
    }

    /**

     Returns the IP address of the current leader of the database cluster.
     If the leader is not set, it returns a constant string indicating an empty leader.
     @return A string representing the IP address of the leader of the database cluster or an empty leader constant.
     */
    @GetMapping("/get-leader")
    @ResponseBody
    public String getLeader(){
        System.out.println("TRYING TO GET LEADER");
        if(DBManagerState.DBLeaderIP == null || DBManagerState.DBLeaderIP.isEmpty()) return NetworkConstants.EMPTY_DB_LEADER;
        return DBManagerState.DBLeaderIP;
    }

    /**

     This method is used to set the leader IP address in the DBManagerState.
     @param node a JsonNode object that contains the leader IP address to be set.
     @return void
     */
    @PostMapping("/leader")
    @ResponseBody
    public void setLeader(@RequestBody JsonNode node){
        String leaderIP = node.get("leaderIP").asText();
        DBManagerState.DBLeaderIP = leaderIP;
        return;
    }

    /**

     Handles a GET request to "/notify-leader", which broadcasts the status of the current database cluster to the leader of the cluster.
     The method creates a new ObjectMapper object, creates a JsonNode object, sets the "isFirstClusterPrimary" field of the JsonNode object to the
     value of the "isFirstClusterPrimary" field of the DB class, and then broadcasts the JsonNode object to all members of the cluster.
     This method does not return anything.
     */
    @GetMapping("/notify-leader")
    @ResponseBody
    public void notifyLeader(){
        //new Thread(new DatabaseClusterMonitor()).start();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rq = objectMapper.createObjectNode();
        ((ObjectNode)rq).put("isFirstClusterPrimary", DB.isFirstClusterPrimary);
        NetworkUtil.broadcastPrimaryReplica(rq);
        return;
    }

    /**

     An HTTP POST method used for broadcasting the primary replica state of a database cluster.
     @param node a JsonNode object that represents the request body sent in the HTTP POST request.

     @return This method returns void. It simply updates the "isFirstClusterPrimary" field in the "DB" class
     */
    @PostMapping("/broadcast-primary")
    public void broadcastPrimary(@RequestBody JsonNode node) {
        DB.isFirstClusterPrimary = node.get("isFirstClusterPrimary").asBoolean();
    }

//    @GetMapping("/get-primary")
//    public String getPrimary() {
//        DB db = new DB();
//        if (DB.isFirstClusterPrimary == null) {
//            db.setIsFirstClusterPrimary();
//        }
//
//        return Boolean.toString(DB.isFirstClusterPrimary);
//    }
//
//    @GetMapping("/set-primary/{newIsFirstClusterPrimary}")
//    public void setPrimary(@PathVariable String newIsFirstClusterPrimary) {
//        DB db = new DB();
//        db.updateIsFirstClusterPrimary(Boolean.parseBoolean(newIsFirstClusterPrimary));
//    }

}
