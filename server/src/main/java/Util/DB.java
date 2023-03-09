package Util;

import java.io.*;
import java.util.*;

import MainServer.Models.ClientRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB {
	private final static String URI = "mongodb+srv://cpsc559:cpsc559@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority";
	private final String collectionName = "files_data";
	private final static int numberOfDatabases = 3;
	private static ArrayList<MongoCollection<Document>> databases;
	private ObjectMapper mapper;
	private static int currentPrimaryIndex = -1;

	public static int getCurrentPrimaryIndex() {
		return currentPrimaryIndex;
	}

	public static void setCurrentPrimaryIndex(int currentPrimaryIndex) {
		DB.currentPrimaryIndex = currentPrimaryIndex;
	}

	public static void saveCurrentPrimaryIndexToFile() {
		try (FileWriter writer = new FileWriter("PrimaryIndex")) {
			writer.write(Integer.toString(currentPrimaryIndex));
		} catch (IOException e) {
			System.err.println("Error saving current primary index to file: " + e.getMessage());
		}
	}

	public static void loadLastPrimaryIndexFromFile() {
		try (Scanner scanner = new Scanner(new File("PrimaryIndex"))) {
			int lastPrimaryIndex = scanner.nextInt();
			setCurrentPrimaryIndex(lastPrimaryIndex);
		} catch (IOException e) {
			System.err.println("Error loading last primary index from file: " + e.getMessage());
		}
	}

	private int readDatabaseOffsetFromFile() {
		try (Scanner scanner = new Scanner(new File("DatabaseOffset"))) {
			return scanner.nextInt();
		} catch (IOException e) {
			System.err.println("Error reading database offset from file: " + e.getMessage());
		}
	}

	private void writeDatabaseOffsetToFile(int databaseOffset) {
		try (FileWriter writer = new FileWriter("DatabaseOffset")) {
			writer.write(Integer.toString(databaseOffset));
		} catch (IOException e) {
			System.err.println("Error writing database offset to file: " + e.getMessage());
		}
	}

	private String generateDatabaseName() {
		int nextOffset = readDatabaseOffsetFromFile() + 1;
		writeDatabaseOffsetToFile(nextOffset);
		return (String.format("cpsc559_db_%s", nextOffset));
	}

	private void setNextPrimaryIndex() {
		setCurrentPrimaryIndex((getCurrentPrimaryIndex() + 1) % databases.size());
	}

	private MongoCollection<Document> getPrimaryReplica() {
		return databases.get(getCurrentPrimaryIndex());
	}

	public DB() {
		MongoClient mongoClient = MongoClients.create(URI);

		if (databases == null) {
			for (int i = 0; i < numberOfDatabases; i++) {
				databases.add(mongoClient.getDatabase(generateDatabaseName()).getCollection(collectionName));
			}
		}

		if (getCurrentPrimaryIndex() == -1) {
			loadLastPrimaryIndexFromFile();
		}
	}

	private void replicateDatabase(MongoCollection<Document> primaryReplica, MongoCollection<Document> secondaryReplica) {
		List<Document> primaryDocs = primaryReplica.find().into(new ArrayList<>());
		List<ObjectId> primaryIDs = new ArrayList<>();

		for (Document primaryDoc : primaryDocs) {
			ObjectId id = primaryDoc.getObjectId("_id");
			primaryIDs.add(id);
			secondaryReplica.deleteOne(eq("_id", id));
			secondaryReplica.insertOne(primaryDoc);
		}

		secondaryReplica.deleteMany(nin("_id", primaryIDs));
	}

	public Document uploadFile(ClientRequestModel model) throws IOException {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document entry = new Document("_id", new ObjectId())
				.append("filename", model.fileName)
				.append("bytes", model.bytes)
				.append("owner", model.userName)
				.append("created", formattedDate)
				.append("shared", model.shareWith);

		MongoCollection<Document> primaryReplica = getPrimaryReplica();

		try {
			primaryReplica.insertOne(entry);
		} catch (MongoException | IllegalArgumentException e) {
			// the primary replica failed, so we need to elect a new one and make sure it has
			// all the same files, as well as the latest entry
			setNextPrimaryIndex();
			saveCurrentPrimaryIndexToFile();
		}


		return entry;
	}

	public void uploadFile(Document entry) throws IOException {
		int primaryIndex = getCurrentPrimaryIndex();

		for (int i = 0; i < databases.size(); i++) {
			if (i != primaryIndex) {
				databases.get(i).insertOne(entry);
			}
		}
	}

	public ArrayList<JsonNode> findFiles(String ownerName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();
		
		FindIterable<Document> doc = this.filesCollection.find(eq("owner",ownerName));
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
		Document doc = this.filesCollection.find(eq("filename", filename)).first();
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
		UpdateResult updateResult = this.filesCollection.updateOne(filter, updateOperation);

		System.out.println(this.filesCollection.find(filter).first().toJson());
		System.out.println(updateResult);
		//this.filesCollection.findOneAndUpdate({"filename":filename},"shared", Arrays.asList("ragya","sami"));
	}
}