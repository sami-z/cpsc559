package DatabaseManager;

import MainServer.Models.ClientRequestModel;
import Util.DB;
import org.bson.Document;

import java.io.IOException;

public class ReplicationRunner implements Runnable{
    private final Document replicatedEntry;

    public ReplicationRunner(Document replicatedEntry) {
        this.replicatedEntry = replicatedEntry;
    }

    @Override
    public void run() {
        DB db = new DB(true);
        try {
            db.uploadFile(replicatedEntry);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
