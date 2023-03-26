package DatabaseManager;

import MainServer.Models.ClientRequestModel;
import Util.DB;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;

public class ReplicationRunner implements Runnable{
    private final Document replicatedEntry;
    private final ArrayList<String> fileNames;

    public ReplicationRunner(Document replicatedEntry, ArrayList<String> fileNames) {
        this.replicatedEntry = replicatedEntry;
        this.fileNames = fileNames;
    }

    @Override
    public void run() {
        DB db = new DB();
        if (replicatedEntry != null){
            try {
                db.uploadFile(replicatedEntry);
//            db.deleteFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            db.deleteFile(fileNames, true);
        }

    }
}
