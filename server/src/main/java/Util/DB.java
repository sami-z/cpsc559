package Util;

import java.io.*;
import java.util.*;
import MainServer.Models.ClientRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import com.mongodb.client.result.UpdateResult;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB {
	public MongoClient mongoClient1;
	public MongoClient mongoClient2;
	private ObjectMapper mapper;
	public static boolean isFirstClusterPrimary = true;
	public static boolean shouldRecover = false;

	public DB() {
		MongoClientSettings clientSettings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster1.osrr3zu.mongodb.net/?retryWrites=true&w=majority"))
				.applyToSocketSettings(builder ->
						builder.connectTimeout(3, SECONDS))
				.applyToClusterSettings(builder ->
						builder.serverSelectionTimeout(3, SECONDS))
				.build();
		mongoClient1 = MongoClients.create(clientSettings);

		MongoClientSettings clientSettings2 = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority"))
				.applyToSocketSettings(builder ->
						builder.connectTimeout(3, SECONDS))
				.applyToClusterSettings(builder ->
						builder.serverSelectionTimeout(3, SECONDS))
				.build();
		mongoClient2 = MongoClients.create(clientSettings2);

//		this.mongoClient1 = MongoClients.create(DBConstants.MONGO_URI_CLUSTER1);
//		this.mongoClient2 = MongoClients.create(DBConstants.MONGO_URI_CLUSTER2);
//		replicaCluster1 = this.mongoClient1.getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
//		replicaCluster2 = this.mongoClient2.getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
		mapper = new ObjectMapper();
	}

	public MongoClient getPrimaryMongoClient() {
		return (isFirstClusterPrimary) ? mongoClient1 : mongoClient2;
	}

	public MongoClient getSecondaryMongoClient() {
		return (isFirstClusterPrimary) ? mongoClient2 : mongoClient1;
	}

	public MongoDatabase getPrimaryDatabase() {
		return getPrimaryMongoClient().getDatabase(DBConstants.DATABASE_NAME);
	}

	public MongoDatabase getSecondaryDatabase() {
		return getSecondaryMongoClient().getDatabase(DBConstants.DATABASE_NAME);
	}

	public MongoCollection<Document> getPrimaryReplica() {
		return getPrimaryDatabase().getCollection(DBConstants.COLLECTION_NAME);
	}

	public MongoCollection<Document> getSecondaryReplica() {
		return getSecondaryDatabase().getCollection(DBConstants.COLLECTION_NAME);
	}

	public void replicateDatabase() {
		List<Document> primaryDocs = getPrimaryReplica().find().into(new ArrayList<>());
		MongoCollection<Document> secondaryReplica = getSecondaryReplica();
		secondaryReplica.drop();

		for (Document primaryDoc : primaryDocs) {
			secondaryReplica.insertOne(primaryDoc);
		}
	}

	// TODO implement handling of a case where a file with the same filename as the request already exists under the same account (using username), in which case we must overwrite
	public Document uploadFile(ClientRequestModel model, long timestamp) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document query = new Document("fileName", model.fileName);
		Document queryResult = getPrimaryReplica().find(query).first();
		Document entry;

		if (queryResult == null) {
			entry = new Document("_id", new ObjectId())
					.append("fileName", model.fileName)
					.append("bytes", model.bytes)
					.append("userName", model.userName)
					.append("created", formattedDate)
					.append("shared", model.shareWith)
					.append("timestamp", timestamp);
		} else {
			Bson deleteFilter = Filters.eq("_id", queryResult.getObjectId("_id"));
			getPrimaryReplica().deleteOne(deleteFilter);

			entry = new Document("_id", queryResult.getObjectId("_id"))
					.append("fileName", model.fileName)
					.append("bytes", model.bytes)
					.append("userName", model.userName)
					.append("created", formattedDate)
					.append("shared", model.shareWith)
					.append("timestamp", timestamp);
		}

		try {
			getPrimaryReplica().insertOne(entry);
		} catch (Exception e) {
			// TODO implement fault tolerance for clusters
			System.out.println("MongoDB Atlas Primary Cluster is down in DB");

			DB.shouldRecover = true;
			if (DB.isFirstClusterPrimary) {
				DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
			}
			getPrimaryReplica().insertOne(entry);
		}

		return entry;
	}

	public void uploadFile(Document entry) throws IOException {
		String fileName = entry.getString("fileName");
		long entryTimestamp = entry.getLong("timestamp");

		RestTemplate restTemplate = new RestTemplate();
		String getHeadURI = NetworkConstants.getDBManagerGetHeadURI() + "?fileName=" + fileName;

		long latestTimestamp = restTemplate.getForObject(getHeadURI, Long.class);

		if (entryTimestamp >= latestTimestamp) {
			try {
				getSecondaryReplica().insertOne(entry);
			} catch (Exception e) {
				System.out.println("Secondary cluster is currently down in DB");
			}
		}
	}

	public ArrayList<JsonNode> findFiles(String userName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();
		FindIterable<Document> docs;
		try {
			docs = getPrimaryReplica().find(eq("userName",userName));
		} catch (Exception e) {
			DB.shouldRecover = true;
			if (DB.isFirstClusterPrimary) {
				DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
			}
			docs = getPrimaryReplica().find(eq("userName",userName));
		}

		mapper = new ObjectMapper();

//		FindIterable<Document> docs = getPrimaryReplica().find(eq("userName",userName));
		if (docs.iterator().hasNext()) {
			System.out.println("Found files for " + userName + "!");
			for(Document d: docs) {
				JsonNode tempJson = (JsonNode) mapper.readTree(d.toJson());
				ret.add(tempJson);
				System.out.println(">" + tempJson.get("filename"));
			}
		} else {
			System.out.println("No match");
		}
		return ret;
	}


	public JsonNode loadFile(String filename) throws IOException {

		Document doc = getPrimaryReplica().find(eq("fileName", filename)).first();
		if (doc != null) {
			return mapper.readTree(doc.toJson());
//	    	byte[] fileBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(mapper.writeValueAsString(json.get("bytes")));
//	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
//	        FileOutputStream fos = new FileOutputStream(dest);
//	        fos.write(fileBytes);
//	        fos.close();
//	        System.out.println("Saved file at location: " + dest);
	        }
        else {
            System.out.println("File not found");
        }
        return null;
	}
	public void editSharedWith(String fileName, ArrayList<String> sharedList){
		Bson filter = eq("fileName", fileName);
		Bson updateOperation = set("shared", sharedList);
		UpdateResult updateResult = getPrimaryReplica().updateOne(filter, updateOperation);

		System.out.println(getPrimaryReplica().find(filter).first().toJson());
		System.out.println(updateResult);
	}

	public ArrayList<String> deleteFile(ArrayList<String> files, boolean isReplicating){

		ArrayList<String> arr = new ArrayList<>();
		DeleteResult updateResult;
		for (String fileName : files){
			if (!isReplicating){
				updateResult = getPrimaryReplica().deleteOne(eq("fileName", fileName));
			}
			else{
				updateResult = getSecondaryReplica().deleteOne(eq("fileName", fileName));
			}

			if (updateResult.getDeletedCount() == 1){
				arr.add(fileName);
			}
		}

		return arr;
	}

	public void deleteFile(){

	}
}