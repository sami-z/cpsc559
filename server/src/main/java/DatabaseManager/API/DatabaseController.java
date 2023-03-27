package DatabaseManager.API;

import DatabaseManager.ReplicationRunner;
import DatabaseManager.Service.DatabaseHandler;
import MainServer.Models.ClientRequestModel;
import Util.DB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/dbmanager")
public class DatabaseController {
    private final DatabaseHandler databaseHandler;
    private final DB db;

    @Autowired
    public DatabaseController(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
        this.db = new DB();
    }

    @PostMapping("/upload")
    public String uploadToDatabase(@RequestBody ClientRequestModel requestModel) {
        System.out.println("hello i am here: " + requestModel.fileName);
        long timestamp = System.currentTimeMillis();
        databaseHandler.updateTimestamp(requestModel.fileName, timestamp);
        ArrayList<Document> docs = db.uploadFile(requestModel, timestamp);
        new Thread(new ReplicationRunner(docs.get(0), null, null, null, true, false, false)).start();
        if (docs.get(1) == null) {
            return Boolean.toString(false);
        } else {
            return Boolean.toString(true);
        }
    }

    @PostMapping("/delete")
    public String deleteFromDatabase(@RequestBody JsonNode deleteRequest) {
        ArrayList<String> filesToDelete = new ObjectMapper().convertValue(deleteRequest.get("filesToDelete"), ArrayList.class);
        ArrayList<ArrayList<String>> tsList = new ArrayList<>();
        for (String fileName : filesToDelete) {
            long timestamp = System.currentTimeMillis();
            databaseHandler.updateTimestamp(fileName, timestamp);

            ArrayList<String> innerTSList = new ArrayList<>();
            innerTSList.add(fileName);
            innerTSList.add(Long.toString(timestamp));
            tsList.add(innerTSList);
        }
        String deletedFiles = db.deleteFile(filesToDelete);
        new Thread(new ReplicationRunner(null, null, null, tsList, false, false, false)).start();
        return deletedFiles;
    }

    @PostMapping("/share")
    public void editShare(@RequestBody JsonNode shareRequest) {
        String fileName = shareRequest.get("fileName").asText();
        ArrayList<String> shareList = new ObjectMapper().convertValue(shareRequest.get("sharedWith"), ArrayList.class);
        db.editSharedWith(fileName, shareList);
        new Thread(new ReplicationRunner(null, shareList, fileName, null, false, false, true)).start();
    }

    @GetMapping("/get-head/{fileName}")
    @ResponseBody
    public String getTimestamp(@PathVariable String fileName) {
        return Long.toString(databaseHandler.getTimestamp(fileName));
    }

   @PostMapping("/register")
   @ResponseBody
   public String registerUser(@RequestBody ClientRequestModel requestModel) {
        Document replicatedEntry = db.registerUser(requestModel);
        if (replicatedEntry == null) {
            return Boolean.toString(false);
        }
        new Thread(new ReplicationRunner(replicatedEntry, null, null, null,false, true, false)).start();
       return Boolean.toString(true);
    }
}
