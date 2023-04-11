package DatabaseManager;

import Util.DB;
import Util.NetworkUtil;
import com.mongodb.client.MongoClient;
import org.bson.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseClusterMonitor implements Runnable{
    public DB DBInstance;

    public DatabaseClusterMonitor() {
        this.DBInstance = new DB();
    }

    /**

     The DBClusterMonitor class is responsible for monitoring the MongoDB Atlas clusters and ensuring their availability.
     It implements the Runnable interface and runs indefinitely, checking the status of the primary cluster and the need for recovery
     in case of failure. If the primary cluster is down, it triggers a recovery process to promote a secondary node to the new primary.
     It also sets the logging levels of MongoDB drivers to WARNING to avoid flooding the console with log messages.
     */
    @Override
    public void run() {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
        Logger.getLogger("com.mongodb").setLevel(Level.WARNING);

        MongoClient secondaryMongoClient = null;

        while (true) {
            if (DB.shouldRecover) {
                try {
                    if (secondaryMongoClient == null) {
                        secondaryMongoClient = DB.createMongoClient(false);
                    }
//                    secondaryMongoClient = DB.createMongoClient(false);

                    BsonDocument secondaryReplStatus = secondaryMongoClient.getDatabase("admin").runCommand(new BsonDocument("replSetGetStatus", new BsonInt32(1))).toBsonDocument();
                    BsonArray secondaryMembers = secondaryReplStatus.getArray("members");

                    for (BsonValue member : secondaryMembers) {
                        BsonDocument secondaryMemberStatus = member.asDocument();
                        String secondaryStateStr = secondaryMemberStatus.getString("stateStr").getValue();

                        if (secondaryStateStr.equals("PRIMARY")) {
                            BsonDouble secondaryCurrHeartbeat = (BsonDouble) secondaryMemberStatus.get("health");
                            if (secondaryCurrHeartbeat.doubleValue() == 1.0) {
                                System.out.println("Detected a change in secondary node's heartbeat");

                                DBInstance.replicateDatabase();
                                NetworkUtil.DBManagerNotifyPrimaryChange(true, false);
                                NetworkUtil.processingServerNotifyPrimaryChange(false);
                                secondaryMongoClient.close();
                                secondaryMongoClient = null;
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("MongoDB Atlas Secondary Cluster is still down in DB Cluster Monitor");
                }
            }
        }
    }
}
