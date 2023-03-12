package DatabaseManager;

import Util.DB;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

public class DatabaseClusterMonitor implements Runnable{
    public DB DBInstance;

    public DatabaseClusterMonitor() {
        this.DBInstance = new DB();
    }

    @Override
    public void run() {
        BsonValue prevHeartbeat = null;

        while (true) {
            MongoDatabase primaryDatabase = DBInstance.getPrimaryDatabase();
            BsonDocument replStatus = primaryDatabase.runCommand(new BsonDocument("replSetGetStatus", new BsonInt32(1))).toBsonDocument();
            BsonArray members = replStatus.getArray("members");

            for (BsonValue member : members) {
                BsonDocument memberStatus = member.asDocument();
                String stateStr = memberStatus.getString("stateStr").getValue();

                if (stateStr.equals("PRIMARY")) {
                    BsonValue currHeartbeat = memberStatus.get("lastHeartbeat");
                    if (prevHeartbeat != null && !prevHeartbeat.equals(currHeartbeat)) {
                        System.out.println("Detected a change in primary node's heartbeat");

                        DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
                        MongoDatabase newPrimaryDatabase = DBInstance.getPrimaryDatabase();
                    }
                    prevHeartbeat = currHeartbeat;
                    break;
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
