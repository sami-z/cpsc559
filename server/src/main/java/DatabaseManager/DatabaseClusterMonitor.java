package DatabaseManager;

import Util.DB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.*;

public class DatabaseClusterMonitor implements Runnable{
    public DB DBInstance;
    public MongoDatabase primaryDatabase;
    public MongoDatabase primaryAdminDatabase;
    public MongoDatabase secondaryDatabase;
    public MongoDatabase secondaryAdminDatabase;

    public DatabaseClusterMonitor() {
        this.DBInstance = new DB();
        primaryDatabase = DBInstance.getPrimaryDatabase();
        secondaryDatabase = DBInstance.getSecondaryDatabase();
        primaryAdminDatabase = DBInstance.getPrimaryAdminDatabase();
        secondaryAdminDatabase = DBInstance.getSecondaryAdminDatabase();
    }

    @Override
    public void run() {
        BsonValue prevHeartbeat = null;
        BsonValue prevPrimaryHeartbeat = null;
        Boolean shouldRecover = false;

        while (true) {
            BsonDocument replStatus = primaryAdminDatabase.runCommand(new BsonDocument("replSetGetStatus", new BsonInt32(1))).toBsonDocument();
            BsonArray members = replStatus.getArray("members");

            for (BsonValue member : members) {
                BsonDocument memberStatus = member.asDocument();
                String stateStr = memberStatus.getString("stateStr").getValue();

                if (stateStr.equals("PRIMARY")) {
                    BsonValue currHeartbeat = memberStatus.get("lastHeartbeat");
                    if (prevHeartbeat != null && !prevHeartbeat.equals(currHeartbeat)) {
                        System.out.println("Detected a change in primary node's heartbeat");

                        shouldRecover = true;
                        DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
                        primaryDatabase = DBInstance.getPrimaryDatabase();
                        secondaryDatabase = DBInstance.getSecondaryDatabase();
                        prevPrimaryHeartbeat = prevHeartbeat;
                        prevHeartbeat = null;
                        break;
                    }
                    prevHeartbeat = currHeartbeat;
                    break;
                }
            }

            // Check if the previously known primary node is back up
            if (shouldRecover) {
                BsonDocument secondaryReplStatus = secondaryAdminDatabase.runCommand(new BsonDocument("replSetGetStatus", new BsonInt32(1))).toBsonDocument();
                BsonArray secondaryMembers = secondaryReplStatus.getArray("members");

                for (BsonValue member : secondaryMembers) {
                    BsonDocument secondaryMemberStatus = member.asDocument();
                    String secondaryStateStr = secondaryMemberStatus.getString("stateStr").getValue();

                    if (secondaryStateStr.equals("PRIMARY")) {
                        BsonValue secondaryCurrHeartbeat = secondaryMemberStatus.get("lastHeartbeat");
                        if (prevPrimaryHeartbeat.equals(secondaryCurrHeartbeat)) {
                            System.out.println("Detected a change in secondary node's heartbeat");

                            MongoCollection<Document> primaryCollection = DBInstance.getPrimaryReplica();
                            MongoCollection<Document> secondaryCollection = DBInstance.getSecondaryReplica();

                            secondaryCollection.drop();
                            DBInstance.replicateDatabase(primaryCollection, secondaryCollection);

                            prevPrimaryHeartbeat = null;
                            shouldRecover = false;
                            break;
                        }
                    }
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
