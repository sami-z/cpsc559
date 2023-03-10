package Util;

import java.io.*;
import java.util.*;

import MainServer.Models.ClientRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB {
	public MongoClient mongoClient1;
	public MongoClient mongoClient2;
	public static MongoCollection<Document> replicaCluster1;
	public static MongoCollection<Document> replicaCluster2;
	private ObjectMapper mapper;
	public static boolean isFirstClusterPrimary = true;

	public DB() {
		this.mongoClient1 = MongoClients.create(DBConstants.MONGO_URI_CLUSTER1);
		this.mongoClient2 = MongoClients.create(DBConstants.MONGO_URI_CLUSTER2);
		replicaCluster1 = this.mongoClient1.getDatabase("cpsc559_db").getCollection("files_data");
		replicaCluster2 = this.mongoClient2.getDatabase("cpsc559_db").getCollection("files_data");


//		if (databases == null) {
//			int beginOffset = readDatabaseOffsetFromFile() - DBConstants.NUMBER_OF_DATABASES;
//			for (int i = 0; i < DBConstants.NUMBER_OF_DATABASES; i++) {
//				beginOffset++;
//				databases.add(mongoClient.getDatabase(generateDatabaseName(beginOffset)));
//			}
//		}
//
//		if (getCurrentPrimaryIndex() == -1) {
//			loadLastPrimaryIndexFromFile();
//		}
	}

	private MongoCollection<Document> getPrimaryReplica() {
		if (isFirstClusterPrimary) {
			return replicaCluster1;
		} else {
			return replicaCluster2;
		}
	}

//	private void replicateDatabase(MongoCollection<Document> primaryReplica, MongoCollection<Document> secondaryReplica) {
//		List<Document> primaryDocs = primaryReplica.find().into(new ArrayList<>());
//
//		for (Document primaryDoc : primaryDocs) {
//			secondaryReplica.insertOne(primaryDoc);
//		}
//	}

	// TODO implement handling of a case where a file with the same filename as the request already exists under the same account (using username), in which case we must overwrite
	public Document uploadFile(ClientRequestModel model) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document entry = new Document("_id", new ObjectId())
				.append("filename", model.fileName)
				.append("bytes", model.bytes)
				.append("owner", model.userName)
				.append("created", formattedDate)
				.append("shared", model.shareWith);

		try {
			getPrimaryReplica().insertOne(entry);
		} catch (MongoException e) {
			// TODO implement fault tolerance for clusters
		}

//		MongoCollection<Document> primaryReplica = getPrimaryReplicaCollection();
//
//		try {
//			primaryReplica.insertOne(entry);
//		} catch (MongoException | IllegalArgumentException e) {
//			int oldPrimaryIndex = getCurrentPrimaryIndex();
//			getPrimaryReplica().drop();
//			databases.set(oldPrimaryIndex, mongoClient.getDatabase(generateDatabaseName()));
//			setNextPrimaryIndex();
//			writeCurrentPrimaryIndexToFile();
//			MongoCollection<Document> newPrimaryReplica = getPrimaryReplicaCollection();
//			replicateDatabase(newPrimaryReplica, databases.get(oldPrimaryIndex).getCollection(DBConstants.COLLECTION_NAME));
//			newPrimaryReplica.insertOne(entry);
//		}

		return entry;
	}

	public void uploadFile(Document entry) throws IOException {
		if (isFirstClusterPrimary) {
			replicaCluster2.insertOne(entry);
		} else {
			replicaCluster1.insertOne(entry);
		}
//		int primaryIndex = getCurrentPrimaryIndex();
//
//		for (int i = 0; i < DBConstants.NUMBER_OF_DATABASES - 1; i++) {
//			primaryIndex = (primaryIndex + 1) % DBConstants.NUMBER_OF_DATABASES;
//			databases.get(primaryIndex).getCollection(DBConstants.COLLECTION_NAME).insertOne(entry);
//		}
	}

	public ArrayList<JsonNode> findFiles(String ownerName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();
		
		FindIterable<Document> doc = getPrimaryReplica().find(eq("owner",ownerName));
		if (doc != null) {
			System.out.println("Found files for " + ownerName + "!");
			for(Document d: doc) {
				JsonNode tempJson = (JsonNode) mapper.readTree(d.toJson());
	        	ret.add(tempJson);
	        	System.out.println(">" + tempJson.get("filename"));
	        }
        } else {
            System.out.println("No match");
        }
		return ret;
	}


	public void saveFileFromDB(String filename, String dest) throws IOException {
		Document doc = getPrimaryReplica().find(eq("filename", filename)).first();
		if (doc != null) {
	    	JsonNode json = (JsonNode) mapper.readTree(doc.toJson());
	    	System.out.println("bytes: "+ json.get("bytes") + " end");
	    	byte[] fileBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(mapper.writeValueAsString(json.get("bytes")));
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	        //ObjectOutputStream os = new ObjectOutputStream(out);
	        //os.writeObject(json.get("selectedFile"));
	        FileOutputStream fos = new FileOutputStream(dest);
	        fos.write(fileBytes);
	        fos.close();
	        System.out.println("Saved file at location: " + dest);
	        }
        else {
            System.out.println("File not found");
        }
        
	}
	public void editSharedWith(String filename){
		Bson filter = eq("filename", filename);
		Bson updateOperation = set("shared", Arrays.asList("ragya","sami","testingUser"));
		UpdateResult updateResult = getPrimaryReplica().updateOne(filter, updateOperation);

		System.out.println(getPrimaryReplica().find(filter).first().toJson());
		System.out.println(updateResult);
		//this.filesCollection.findOneAndUpdate({"filename":filename},"shared", Arrays.asList("ragya","sami"));
	}

	//	private int getCurrentPrimaryIndex() {
//		return currentPrimaryIndex;
//	}
//
//	private void setCurrentPrimaryIndex(int currentPrimaryIndex) {
//		DB.currentPrimaryIndex = currentPrimaryIndex;
//	}
//
//	private void loadLastPrimaryIndexFromFile() {
//		try (Scanner scanner = new Scanner(new File("Util/" + DBConstants.INDEX_FILE_NAME))) {
//			setCurrentPrimaryIndex(scanner.nextInt());
//		} catch (IOException e) {
//			System.err.println("Error loading last primary index from file: " + e.getMessage());
//		}
//	}
//
//	private void writeCurrentPrimaryIndexToFile() {
//		try (FileWriter writer = new FileWriter("Util/" + DBConstants.INDEX_FILE_NAME)) {
//			writer.write(Integer.toString(currentPrimaryIndex));
//		} catch (IOException e) {
//			System.err.println("Error saving current primary index to file: " + e.getMessage());
//		}
//	}
//
//	private int readDatabaseOffsetFromFile() {
//		try (Scanner scanner = new Scanner(new File("Util/" + DBConstants.OFFSET_FILE_NAME))) {
//			return scanner.nextInt();
//		} catch (IOException e) {
//			System.err.println("Error reading database offset from file: " + e.getMessage());
//		}
//		return 0;
//	}
//
//	private void writeDatabaseOffsetToFile(int databaseOffset) {
//		try (FileWriter writer = new FileWriter("Util/" + DBConstants.OFFSET_FILE_NAME)) {
//			writer.write(Integer.toString(databaseOffset));
//		} catch (IOException e) {
//			System.err.println("Error writing database offset to file: " + e.getMessage());
//		}
//	}
//
//	private String generateDatabaseName() {
//		int nextOffset = readDatabaseOffsetFromFile() + 1;
//		writeDatabaseOffsetToFile(nextOffset);
//		return (String.format("cpsc559_db_%s", nextOffset));
//	}
//
//	private String generateDatabaseName(int offset) {
//		return (String.format("cpsc559_db_%s", offset));
//	}
//
//	private void setNextPrimaryIndex() {
//		setCurrentPrimaryIndex((getCurrentPrimaryIndex() + 1) % DBConstants.NUMBER_OF_DATABASES);
//	}
//
//	private MongoDatabase getPrimaryReplica() {
//		return databases.get(getCurrentPrimaryIndex());
//	}
//
//	private MongoCollection<Document> getPrimaryReplicaCollection() {
//		return getPrimaryReplica().getCollection(DBConstants.COLLECTION_NAME);
//	}
}