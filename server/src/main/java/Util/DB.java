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
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.client.result.UpdateResult;

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
				.build();
		mongoClient1 = MongoClients.create(clientSettings);

		MongoClientSettings clientSettings2 = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString("mongodb+srv://admin:123@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority"))
				.applyToSocketSettings(builder ->
						builder.connectTimeout(3, SECONDS))
				.build();
		mongoClient2 = MongoClients.create(clientSettings2);
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
	public Document uploadFile(ClientRequestModel model) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document entry = new Document("_id", new ObjectId())
				.append("fileName", model.fileName)
				.append("bytes", model.bytes)
				.append("userName", model.userName)
				.append("created", formattedDate)
				.append("shared", model.shareWith);

		try {
			getPrimaryReplica().insertOne(entry);
		} catch (Exception e) {
			// TODO implement fault tolerance for clusters
			System.out.println("MongoDB Atlas Primary Cluster is down in DB");
			e.printStackTrace();

			DB.shouldRecover = true;
			if (DB.isFirstClusterPrimary) {
				DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
			}
			getPrimaryReplica().insertOne(entry);
		}

		return entry;
	}

	public void uploadFile(Document entry) throws IOException {
		try {
			getSecondaryReplica().insertOne(entry);
		} catch (Exception e) {
			System.out.println("Secondary cluster is currently down in DB");
			e.printStackTrace();
		}
	}

	public ArrayList<JsonNode> findFiles(String userName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();
		FindIterable<Document> docs = getPrimaryReplica().find(eq("userName",userName));
		mapper = new ObjectMapper();
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
		Document doc = getPrimaryReplica().find(eq("filename", filename)).first();
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
	public void editSharedWith(String filename){
		Bson filter = eq("filename", filename);
		Bson updateOperation = set("shared", Arrays.asList("ragya","sami","testingUser"));
		UpdateResult updateResult = getPrimaryReplica().updateOne(filter, updateOperation);

		System.out.println(getPrimaryReplica().find(filter).first().toJson());
		System.out.println(updateResult);
		//this.filesCollection.findOneAndUpdate({"filename":filename},"shared", Arrays.asList("ragya","sami"));
	}
}