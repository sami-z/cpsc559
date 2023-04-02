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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/api/dbmanager")
public class DatabaseController {
    private final DatabaseHandler databaseHandler;

    @Autowired
    public DatabaseController(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
        DBManagerState.DBLeaderIP = "";
    }

    @PostMapping("/upload")
    public String uploadToDatabase(@RequestBody ClientRequestModel requestModel) {
        DB db = new DB();
        System.out.println("hello i am here: " + requestModel.fileName);
        Document query = db.createUploadQuery(requestModel.userName, requestModel.fileName);
        Document queryResult = db.getReplica(true).find(query).first();
        String ownerName;

        if (queryResult == null) {
            ownerName = requestModel.userName;
        } else {
            ownerName = queryResult.getString("userName");
        }

        long timestamp = System.currentTimeMillis();
        databaseHandler.updateTimestamp(ownerName, requestModel.fileName, timestamp);
        ArrayList<Document> docs = db.uploadFile(requestModel, timestamp, queryResult);
        new Thread(new ReplicationRunner(docs.get(0), null, null, null,0,null, true, false, false)).start();
        db.closeMongoClients();
        if (docs.get(1) == null) {
            return Boolean.toString(false);
        } else {
            return Boolean.toString(true);
        }
    }

    @PostMapping("/delete")
    public String deleteFromDatabase(@RequestBody JsonNode deleteRequest) {
        DB db = new DB();
        ArrayList<String> filesToDelete = new ObjectMapper().convertValue(deleteRequest.get("filesToDelete"), ArrayList.class);
        String userName = deleteRequest.get("userName").asText();
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
        new Thread(new ReplicationRunner(null, null, null, userName,0, tsList, false, false, false)).start();
        db.closeMongoClients();
        return deletedFiles;
    }

    @PostMapping("/share")
    public void editShare(@RequestBody JsonNode shareRequest) {
        DB db = new DB();
        String userName = shareRequest.get("userName").asText();
        ArrayList<String> shareList = new ObjectMapper().convertValue(shareRequest.get("shareWith"), ArrayList.class);
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
        new Thread(new ReplicationRunner(null, shareList, filesToShare, userName,0, tsList, false, false, true)).start();
        db.closeMongoClients();
    }

    @GetMapping("/get-head/{key}")
    @ResponseBody
    public String getTimestamp(@PathVariable String key) {
        return Long.toString(databaseHandler.getTimestamp(key));
    }

   @PostMapping("/register")
   @ResponseBody
   public String registerUser(@RequestBody ClientRequestModel requestModel) {
        DB db = new DB();
        Document replicatedEntry = db.registerUser(requestModel);
        if (replicatedEntry == null) {
            db.closeMongoClients();
            return Boolean.toString(false);
        }
        new Thread(new ReplicationRunner(replicatedEntry, null, null, null,0, null,false, true, false)).start();
        db.closeMongoClients();
        return Boolean.toString(true);
    }

    @GetMapping("/ping")
    public void ping(){
        return;
    }

    @GetMapping("/get-leader")
    @ResponseBody
    public String getLeader(){
        System.out.println("TRYING TO GET LEADER");
        return DBManagerState.DBLeaderIP == null ? "" : DBManagerState.DBLeaderIP;
    }

    @PostMapping("/leader")
    @ResponseBody
    public void setLeader(@RequestBody JsonNode node){
        String leaderIP = node.get("leaderIP").asText();
        DBManagerState.DBLeaderIP = leaderIP;
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String ip = address.getHostAddress();
        if (ip.equals(DBManagerState.DBLeaderIP)) {
            new Thread(new DatabaseClusterMonitor()).start();
        }
    }

    @GetMapping("/get-primary")
    public String getPrimary() {
        DB db = new DB();
        if (DB.isFirstClusterPrimary == null) {
            db.setIsFirstClusterPrimary();
        }

        return Boolean.toString(DB.isFirstClusterPrimary);
    }

    @GetMapping("/set-primary/{newIsFirstClusterPrimary}")
    public void setPrimary(@PathVariable String newIsFirstClusterPrimary) {
        DB db = new DB();
        db.updateIsFirstClusterPrimary(Boolean.parseBoolean(newIsFirstClusterPrimary));
    }

}
