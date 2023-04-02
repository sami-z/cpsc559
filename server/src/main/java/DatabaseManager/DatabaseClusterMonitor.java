package DatabaseManager;

import Util.DB;
import com.mongodb.client.MongoClient;
import org.bson.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseClusterMonitor implements Runnable{
    public DB DBInstance;

    public DatabaseClusterMonitor() {
        this.DBInstance = new DB();
    }

    @Override
    public void run() {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);
        Logger.getLogger("com.mongodb").setLevel(Level.WARNING);

        DB db = new DB();

        while (true) {
            try {
                MongoClient primaryMongoClient = DBInstance.createMongoClient(true);
                primaryMongoClient.close();
            } catch (Exception e) {
                System.out.println("MongoDB Atlas Primary Cluster is down in DB Cluster Monitor");

                db.recoverFromDatabaseFailure();
            }

            if (DB.shouldRecover) {
                try {
                    MongoClient secondaryMongoClient = DBInstance.createMongoClient(false);

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

                                DB.shouldRecover = false;
                                break;
                            }
                        }
                    }

                    secondaryMongoClient.close();

                } catch (Exception e) {
                    System.out.println("MongoDB Atlas Secondary Cluster is still down in DB Cluster Monitor");
                }
            }
        }
    }
}
