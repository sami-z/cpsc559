package DatabaseManager;

import MainServer.Models.ClientRequestModel;
import Util.DB;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DatabaseController {


    @PostMapping("/dbmanager/upload")
    public void uploadToDatabase(@RequestBody ClientRequestModel requestModel) throws InterruptedException, IOException {
        DB db = new DB();
        Document replicatedEntry = db.uploadFile(requestModel);
        new Thread(new ReplicationRunner(replicatedEntry)).start();
    }
}
