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
		this.mongoClient1 = createMongoClient(true);
		this.mongoClient2 = createMongoClient(false);
		this.mapper = new ObjectMapper();
	}

	public MongoClient createMongoClient(boolean shouldGetPrimary) {
		String URI;
		if (shouldGetPrimary) {
			URI = (DB.isFirstClusterPrimary) ? DBConstants.MONGO_URI_CLUSTER1 : DBConstants.MONGO_URI_CLUSTER2;
		} else {
			URI = (DB.isFirstClusterPrimary) ? DBConstants.MONGO_URI_CLUSTER2 : DBConstants.MONGO_URI_CLUSTER1;
		}
		MongoClientSettings clientSettings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(URI))
				.applyToSocketSettings(builder ->
						builder.connectTimeout(3, SECONDS))
				.applyToClusterSettings(builder ->
						builder.serverSelectionTimeout(3, SECONDS))
				.build();
		return MongoClients.create(clientSettings);
	}

	public MongoClient getMongoClient(boolean shouldGetPrimary) {
		if (shouldGetPrimary) {
			return (isFirstClusterPrimary) ? this.mongoClient1 : this.mongoClient2;
		} else {
			return (isFirstClusterPrimary) ? this.mongoClient2 : this.mongoClient1;
		}
	}

	public MongoCollection<Document> getReplica(boolean shouldGetPrimary) {
		if (shouldGetPrimary) {
			return getMongoClient(true).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
		} else {
			return getMongoClient(false).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
		}
	}

	public void replicateDatabase() {
		List<Document> primaryDocs = getReplica(true).find().into(new ArrayList<>());
		MongoCollection<Document> secondaryReplica = getReplica(false);
		secondaryReplica.drop();

		for (Document primaryDoc : primaryDocs) {
			secondaryReplica.insertOne(primaryDoc);
		}
	}

	public Document createEntry(ClientRequestModel model, long timestamp, ObjectId id, String formattedDate) {
		Document entry;
		if (id == null) {
			entry = new Document("_id", new ObjectId());
		} else {
			entry = new Document("_id", id);
		}

		entry.append("fileName", model.fileName)
				.append("bytes", model.bytes)
				.append("userName", model.userName)
				.append("created", formattedDate)
				.append("shared", model.shareWith)
				.append("timestamp", timestamp);

		return entry;
	}

	public void recoverFromDatabaseFailure() {
		System.out.println("MongoDB Atlas Primary Cluster is down in DB");

		DB.shouldRecover = true;
		if (DB.isFirstClusterPrimary) {
			DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
		}
	}

	public Document uploadFile(ClientRequestModel model, long timestamp) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document query = new Document("fileName", model.fileName);
		Document queryResult = getReplica(true).find(query).first();
		Document entry;

		if (queryResult == null) {
			entry = createEntry(model, timestamp, null, formattedDate);
		} else {
			Bson deleteFilter = Filters.eq("_id", queryResult.getObjectId("_id"));
			try {
				getReplica(true).deleteOne(deleteFilter);
			} catch (Exception e) {
				recoverFromDatabaseFailure();
				getReplica(true).deleteOne(deleteFilter);
			}
			entry = createEntry(model, timestamp, queryResult.getObjectId("_id"), formattedDate);
		}

		try {
			getReplica(true).insertOne(entry);
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			getReplica(true).insertOne(entry);
		}

		return entry;
	}

	public void uploadFile(Document entry) throws IOException {
		String fileName = entry.getString("fileName");
		long entryTimestamp = entry.getLong("timestamp");

		RestTemplate restTemplate = new RestTemplate();
		String getHeadURI = NetworkConstants.getDBManagerGetHeadURI(fileName);

		long latestTimestamp = restTemplate.getForObject(getHeadURI, Long.class);

		if (entryTimestamp >= latestTimestamp) {
			try {
				getReplica(false).insertOne(entry);
			} catch (Exception e) {
				System.out.println("Secondary cluster is currently down in DB");
			}
		}
	}

	public ArrayList<JsonNode> findFiles(String userName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();
		FindIterable<Document> docs;
		try {
			docs = getReplica(true).find(eq("userName",userName));
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			docs = getReplica(true).find(eq("userName",userName));
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
		Document doc;
		try {
			doc = getReplica(true).find(eq("fileName", filename)).first();
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			doc = getReplica(true).find(eq("fileName", filename)).first();
		}
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
		UpdateResult updateResult;
		try {
			updateResult = getReplica(true).updateOne(filter, updateOperation);
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			updateResult = getReplica(true).updateOne(filter, updateOperation);
		}

		System.out.println(getReplica(true).find(filter).first().toJson());
		System.out.println(updateResult);
	}

	public ArrayList<String> deleteFile(ArrayList<String> files, boolean isReplicating) {

		ArrayList<String> arr = new ArrayList<>();
		DeleteResult updateResult;
		for (String fileName : files) {
			if (!isReplicating) {
				try {
					updateResult = getReplica(true).deleteOne(eq("fileName", fileName));
				} catch (Exception e) {
					recoverFromDatabaseFailure();
					updateResult = getReplica(true).deleteOne(eq("fileName", fileName));
				}
			}
			else{
				try {
					updateResult = getReplica(false).deleteOne(eq("fileName", fileName));
				} catch (Exception e) {
					recoverFromDatabaseFailure();
					updateResult = getReplica(false).deleteOne(eq("fileName", fileName));
				}
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