package DatabaseManager;

import Util.DB;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

public class DatabaseClusterMonitor implements Runnable{
    public DB DBInstance;

    public DatabaseClusterMonitor() {
        this.DBInstance = new DB();
    }

    @Override
    public void run() {
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.WARNING);

        while (true) {
            try {
//                System.out.println("in first try: " + DB.isFirstClusterPrimary);
                if (DB.isFirstClusterPrimary) {
                    MongoClientSettings clientSettings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster1.osrr3zu.mongodb.net/?retryWrites=true&w=majority"))
                            .applyToSocketSettings(builder ->
                                    builder.connectTimeout(3, SECONDS))
                            .applyToClusterSettings(builder ->
                                    builder.serverSelectionTimeout(3, SECONDS))
                            .build();
                    MongoClient mongoClient1 = MongoClients.create(clientSettings);
                    mongoClient1.close();
                } else {
                    MongoClientSettings clientSettings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority"))
                            .applyToSocketSettings(builder ->
                                    builder.connectTimeout(3, SECONDS))
                            .applyToClusterSettings(builder ->
                                    builder.serverSelectionTimeout(3, SECONDS))
                            .build();
                    MongoClient mongoClient2 = MongoClients.create(clientSettings);
                    mongoClient2.close();
                }
            } catch (Exception e) {
                System.out.println("MongoDB Atlas Primary Cluster is down in DB Cluster Monitor");

                DB.shouldRecover = true;
                DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
            }

            // Check if the previously known primary node is back up
            if (DB.shouldRecover) {
                try {
                    System.out.println("In should recover: " + DB.isFirstClusterPrimary);
                    MongoClient secondaryMongoClient;
                    if (DB.isFirstClusterPrimary) {
                        MongoClientSettings clientSettings = MongoClientSettings.builder()
                                .applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority"))
                                .applyToSocketSettings(builder ->
                                        builder.connectTimeout(3, SECONDS))
                                .applyToClusterSettings(builder ->
                                        builder.serverSelectionTimeout(3, SECONDS))
                                .build();
                        secondaryMongoClient = MongoClients.create(clientSettings);
                    } else {
                        MongoClientSettings clientSettings = MongoClientSettings.builder()
                                .applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster1.osrr3zu.mongodb.net/?retryWrites=true&w=majority"))
                                .applyToSocketSettings(builder ->
                                        builder.connectTimeout(3, SECONDS))
                                .applyToClusterSettings(builder ->
                                        builder.serverSelectionTimeout(3, SECONDS))
                                .build();
                        secondaryMongoClient = MongoClients.create(clientSettings);
                    }

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
