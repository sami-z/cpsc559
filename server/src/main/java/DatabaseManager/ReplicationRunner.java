package DatabaseManager;

import Util.DB;
import org.bson.Document;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ReplicationRunner implements Runnable{
    private final Document replicatedEntry;
    private final boolean shouldReplicateFile;
    private final boolean shouldReplicateLogin;
    private final boolean shouldReplicateShare;
    private final ArrayList<ArrayList<String>> TSList;
    private final ArrayList<String> shareList;
    private final ArrayList<String> filesToShare;
    private final long timestamp;
    private final String userName;

    public ReplicationRunner(Document replicatedEntry, ArrayList<String> shareList, ArrayList<String> filesToShare, String userName, long timestamp, ArrayList<ArrayList<String>> TSList, boolean shouldReplicateFile, boolean shouldReplicateLogin, boolean shouldReplicateShare) {
        this.replicatedEntry = replicatedEntry;
        this.TSList = TSList;
        this.shouldReplicateFile = shouldReplicateFile;
        this.shouldReplicateLogin = shouldReplicateLogin;
        this.shouldReplicateShare = shouldReplicateShare;
        this.shareList = shareList;
        this.filesToShare = filesToShare;
        this.timestamp = timestamp;
        this.userName = userName;
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
        } else if (shouldReplicateShare) {
            db.editSharedWith(TSList, userName, shareList, timestamp, true);
        } else {
            db.deleteFile(TSList, userName,true);
        }
        db.closeMongoClients();
    }
}
