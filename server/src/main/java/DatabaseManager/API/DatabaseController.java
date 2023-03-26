package DatabaseManager.API;

import DatabaseManager.ReplicationRunner;
import DatabaseManager.Service.DatabaseHandler;
import MainServer.Models.ClientRequestModel;
import Util.DB;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class DatabaseController {
    private final DatabaseHandler databaseHandler;

    @Autowired
    public DatabaseController(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }

    @PostMapping("/dbmanager/upload")
    public void uploadToDatabase(@RequestBody ClientRequestModel requestModel) throws InterruptedException, IOException {
        System.out.println("hello i am here: " + requestModel.fileName);
        DB db = new DB();
        long timestamp = System.currentTimeMillis();
        databaseHandler.updateTimestamp(requestModel.fileName, timestamp);
        Document replicatedEntry = db.uploadFile(requestModel, timestamp);
        new Thread(new ReplicationRunner(replicatedEntry, null)).start();
    }

    @GetMapping("/dbmanager/getHead")
    public long getTimestamp(@RequestParam("fileName") String fileName) {
        return databaseHandler.getTimestamp(fileName);
    }
}
