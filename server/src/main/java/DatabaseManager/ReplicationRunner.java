package DatabaseManager;

import Util.DB;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;

public class ReplicationRunner implements Runnable{
    private final Document replicatedEntry;
    private final boolean shouldReplicateFile;
    private final boolean shouldReplicateLogin;
    private final ArrayList<ArrayList<String>> TSList;

    public ReplicationRunner(Document replicatedEntry, ArrayList<ArrayList<String>> TSList, boolean shouldReplicateFile, boolean shouldReplicateLogin) {
        this.replicatedEntry = replicatedEntry;
        this.TSList = TSList;
        this.shouldReplicateFile = shouldReplicateFile;
        this.shouldReplicateLogin = shouldReplicateLogin;
    }

    @Override
    public void run() {
        DB db = new DB();
        if (shouldReplicateFile) {
            try {
                db.uploadFile(replicatedEntry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (shouldReplicateLogin) {
            db.registerUser(replicatedEntry);
        } else {
            db.deleteFile(TSList, true);
        }

    }
}
