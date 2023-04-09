package Util;

import java.io.*;
import java.util.*;
import MainServer.Models.ClientRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
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
	public static Boolean isFirstClusterPrimary = true;
	public static boolean shouldRecover = false;

	public DB() {
		this.mongoClient1 = createMongoClient(true);
		this.mongoClient2 = createMongoClient(false);
		this.mapper = new ObjectMapper();
	}

	public MongoClient createMongoClient(boolean shouldGetPrimary) {
		String URI;
//		boolean isFirstClusterPrimary = NetworkUtil.getIsFirstClusterPrimary();
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

	public void closeMongoClients() {
		if (this.mongoClient1 != null) {
			this.mongoClient1.close();
		}

		if (this.mongoClient2 != null) {
			this.mongoClient2.close();
		}
	}

	public MongoClient getMongoClient(boolean shouldGetPrimary) {
//		boolean isFirstClusterPrimary = NetworkUtil.getIsFirstClusterPrimary();
		if (shouldGetPrimary) {
			return (DB.isFirstClusterPrimary) ? this.mongoClient1 : this.mongoClient2;
		} else {
			return (DB.isFirstClusterPrimary) ? this.mongoClient2 : this.mongoClient1;
		}
	}

	public MongoCollection<Document> getReplica(boolean shouldGetPrimary) {
		if (shouldGetPrimary) {
			return getMongoClient(true).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
		} else {
			return getMongoClient(false).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.COLLECTION_NAME);
		}
	}

	public MongoCollection<Document> getLoginReplica(boolean shouldGetPrimary) {
		if (shouldGetPrimary) {
			return getMongoClient(true).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.LOGIN_COLLECTION_NAME);
		} else {
			return getMongoClient(false).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.LOGIN_COLLECTION_NAME);
		}
	}

//	public MongoCollection<Document> getIsFirstClusterPrimaryReplica() {
//		return getMongoClient(false).getDatabase(DBConstants.DATABASE_NAME).getCollection(DBConstants.PRIMARY_COLLECTION_NAME);
//	}

//	public Document getAdminPrimaryQuery() {
//		return new Document("userName", "admin");
//	}

//	public void setIsFirstClusterPrimary() {
//		MongoCollection<Document> mc = getIsFirstClusterPrimaryReplica();
//		Document queryResult = mc.find(getAdminPrimaryQuery()).first();
//
//		if (queryResult == null) {
//			Document recordPrimaryDoc = new Document("_id", new ObjectId())
//					.append("userName", "admin")
//					.append("isFirstClusterPrimary", true);
//			mc.insertOne(recordPrimaryDoc);
//		} else {
//			DB.isFirstClusterPrimary = queryResult.getBoolean("isFirstClusterPrimary");
//		}
//	}

//	public void updateIsFirstClusterPrimary(boolean newIsFirstClusterPrimary) {
//		MongoCollection<Document> mc = getIsFirstClusterPrimaryReplica();
//		Document queryResult = mc.find(getAdminPrimaryQuery()).first();
//
//		UpdateResult result = mc.updateOne(
//				queryResult,
//				Updates.set("isFirstClusterPrimary", newIsFirstClusterPrimary)
//		);
//		System.out.println(result);
//
//		if (DB.isFirstClusterPrimary) {
//			DB.isFirstClusterPrimary = newIsFirstClusterPrimary;
//		}
//	}

	public void replicateDatabase() {
		List<Document> primaryDocs = getReplica(true).find().into(new ArrayList<>());
		MongoCollection<Document> secondaryReplica = getReplica(false);
		secondaryReplica.drop();

		for (Document primaryDoc : primaryDocs) {
			secondaryReplica.insertOne(primaryDoc);
		}

		List<Document> primaryLoginDocs = getLoginReplica(true).find().into(new ArrayList<>());
		MongoCollection<Document> secondaryLoginReplica = getLoginReplica(false);
		secondaryLoginReplica.drop();

		for (Document primaryDoc : primaryLoginDocs) {
			secondaryLoginReplica.insertOne(primaryDoc);
		}
	}

	public Document createEntry(ClientRequestModel model, long timestamp, ObjectId id, String formattedDate, String userName, String currentUser) {
		Document entry;
		if (id == null) {
			entry = new Document("_id", new ObjectId());
		} else {
			entry = new Document("_id", id);
		}

		entry.append("fileName", model.fileName)
				.append("bytes", model.bytes)
				.append("currentUser",currentUser)
				.append("userName", userName)
				.append("created", formattedDate)
				.append("shared", String.join(",",model.shared))
				.append("timestamp", timestamp);

		return entry;
	}

	public void recoverFromDatabaseFailure() {
		System.out.println("MongoDB Atlas Primary Cluster is down in DB");
		DB.shouldRecover = true;
		if (DB.isFirstClusterPrimary) {
//			NetworkUtil.setIsFirstClusterPrimary(!DB.isFirstClusterPrimary);
			DB.isFirstClusterPrimary = !DB.isFirstClusterPrimary;
		}
	}

	public Document createUploadQuery(String userName, String fileName) {
		return new Document("$or",
				Arrays.asList(
						new Document("userName", userName),
						new Document("shared", new Document("$regex", ".*" + userName + ".*"))
				))
				.append("fileName", fileName);
	}

	public ArrayList<Document> uploadFile(ClientRequestModel model, long timestamp, Document queryResult) {
		ArrayList<Document> ret = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = currentDate.format(formatter);

		Document entry;
		boolean wasReplaced = false;

		if (queryResult == null) {
			entry = createEntry(model, timestamp, null, formattedDate, model.userName, model.currentUser);
		} else {
			ObjectId existingObjectId = queryResult.getObjectId("_id");
			Bson deleteFilter = Filters.eq("_id", existingObjectId);
			try {
				getReplica(true).deleteOne(deleteFilter);
			} catch (Exception e) {
				recoverFromDatabaseFailure();
				getReplica(true).deleteOne(deleteFilter);
			}
			entry = createEntry(model, timestamp, existingObjectId, formattedDate, queryResult.getString("userName"), queryResult.getString("currentUser"));
			wasReplaced = true;
		}

		try {
			getReplica(true).insertOne(entry);
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			getReplica(true).insertOne(entry);
		}

		ret.add(entry);
		if (wasReplaced) {
			ret.add(new Document("_id", new ObjectId()).append("wasReplaced", "true"));
		} else {
			ret.add(null);
		}

		return ret;
	}

	public void uploadFile(Document entry) throws IOException {
		String fileName = entry.getString("fileName");
		String userName = entry.getString("userName");
		long entryTimestamp = entry.getLong("timestamp");
		String key = String.join(",", userName, fileName);
		long latestTimestamp = NetworkUtil.getTimestamp(key);

		if (entryTimestamp >= latestTimestamp) {
			Document query = createUploadQuery(userName, fileName);

			Document queryResult = getReplica(false).find(query).first();
			if (queryResult == null) {
				getReplica(false).insertOne(entry);
			} else {
				ObjectId existingObjectId = queryResult.getObjectId("_id");
				Bson deleteFilter = Filters.eq("_id", existingObjectId);
				try {
					getReplica(false).deleteOne(deleteFilter);
				} catch (Exception e) {
					System.out.println("Secondary cluster is currently down in DB");
					return;
				}
				try {
					getReplica(false).insertOne(entry);
				} catch (Exception e) {
					System.out.println("Secondary cluster is currently down in DB");
				}
			}
		}
	}

	public Document registerUser(ClientRequestModel model) {
		Document query = new Document("userName", model.currentUser);
		Document queryResult = getLoginReplica(true).find(query).first();
		Document loginDoc;

		if (queryResult == null) {
			loginDoc = new Document("_id", new ObjectId())
					.append("userName", model.currentUser)
					.append("password", model.password);
		} else {
			return null;
		}

		try {
			getLoginReplica(true).insertOne(loginDoc);
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			getLoginReplica(true).insertOne(loginDoc);
		}

		return loginDoc;
	}

	public void registerUser(Document entry) {
		try {
			getLoginReplica(false).insertOne(entry);
		} catch (Exception e) {
			System.out.println("Secondary cluster is currently down in DB");
		}
	}

	public ArrayNode findFiles(String userName) throws JsonProcessingException {
		ArrayList<JsonNode> ret = new ArrayList<>();


// create a new ArrayNode
		ArrayNode arrayNode = new ObjectMapper().createArrayNode();

		FindIterable<Document> docs;
		Document query = new Document("$or",
				Arrays.asList(
						new Document("userName", userName),
						new Document("shared", new Document("$regex", ".*" + userName + ".*"))
				));
		try {
			docs = getReplica(true).find(query);
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			docs = getReplica(true).find(query);
		}

		mapper = new ObjectMapper();

//		FindIterable<Document> docs = getPrimaryReplica().find(eq("userName",userName));
		if (docs.iterator().hasNext()) {
			System.out.println("Found files for " + userName + "!");
			for(Document d: docs) {
				JsonNode tempJson = (JsonNode) mapper.readTree(d.toJson());
				//ret.add(tempJson);
				arrayNode.add(tempJson);
				System.out.println(">" + tempJson.get("filename"));
			}
		} else {
			System.out.println("No match");
		}
		return arrayNode;
	}

	public Bson createUsernameFilenameFilter(String userName, String fileName) {
		return and(eq("fileName", fileName), eq("userName", userName));
	}

	public JsonNode loadFile(String userName, String fileName) throws IOException {
		Document doc;
		Bson filter = createUploadQuery(userName, fileName);
		try {
			doc = getReplica(true).find(filter).first();
		} catch (Exception e) {
			recoverFromDatabaseFailure();
			doc = getReplica(true).find(filter).first();
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

	public Bson createShareOperation(String prevSharedList, ArrayList<String> sharedList) {
		String shareString = String.join(",", sharedList);
		if (prevSharedList.isEmpty())
		{
			return set("shared", shareString);
		}
		return set("shared", prevSharedList + "," + shareString);
	}

	public void editSharedWith(ArrayList<String> filesToShare, String userName, ArrayList<String> sharedList){
		for(String fileName: filesToShare)
		{
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			Document queryResult = getReplica(true).find(filter).first();
			String prevSharedList = queryResult.getString("shared");
			Bson updateOperation = createShareOperation(prevSharedList, sharedList);
			UpdateResult updateResult;

			try {
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			} catch (Exception e) {
				recoverFromDatabaseFailure();
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			}

			System.out.println(updateResult);
		}
	}

	public void editSharedWith(ArrayList<ArrayList<String>> files, String userName, ArrayList<String> sharedList, boolean isReplicating) {
		if (isReplicating) {
			for (ArrayList<String> innerTSList : files) {
				String fileName = innerTSList.get(0);
				String key = String.join(",", userName, fileName);
				long entryTimestamp = Long.parseLong(innerTSList.get(1));
				long latestTimestamp = NetworkUtil.getTimestamp(key);
				if (entryTimestamp >= latestTimestamp) {
					Bson filter = createUsernameFilenameFilter(userName, fileName);
					Document queryResult = getReplica(false).find(filter).first();
					String prevSharedList = queryResult.getString("shared");
					Bson updateOperation = createShareOperation(prevSharedList, sharedList);
					UpdateResult updateResult;

					try {
						updateResult = getReplica(false).updateOne(queryResult, updateOperation);
					} catch (Exception e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}

					System.out.println(updateResult);
				}
			}
		}
	}

	public Bson createUnshareOperation(String prevSharedList, ArrayList<String> unsharedList) {

		String[] shared = prevSharedList.split("\\s*,\\s*");
		ArrayList<String> arrayList = new ArrayList<>();

		for (int i = 0; i < shared.length; i++) {
			arrayList.add(shared[i]);
		}

//		List<String> shared = Arrays.asList(prevSharedList.split("\\s*,\\s*"));
		arrayList.removeAll(unsharedList);
		return set("shared", arrayList.toString());
	}

	public void editUnsharedWith(ArrayList<String> filesToUnShare, String userName, ArrayList<String> unshareList){
		for(String fileName: filesToUnShare)
		{
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			Document queryResult = getReplica(true).find(filter).first();
			String prevSharedList = queryResult.getString("shared");
			Bson updateOperation = createUnshareOperation(prevSharedList, unshareList);
			UpdateResult updateResult;

			try {
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			} catch (Exception e) {
				recoverFromDatabaseFailure();
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			}

			System.out.println(updateResult);
		}
	}

	public void editUnsharedWith(ArrayList<ArrayList<String>> files, String userName, ArrayList<String> unsharedList, boolean isReplicating) {
		if (isReplicating) {
			for (ArrayList<String> innerTSList : files) {
				String fileName = innerTSList.get(0);
				String key = String.join(",", userName, fileName);
				long entryTimestamp = Long.parseLong(innerTSList.get(1));
				long latestTimestamp = NetworkUtil.getTimestamp(key);
				if (entryTimestamp >= latestTimestamp) {
					Bson filter = createUsernameFilenameFilter(userName, fileName);
					Document queryResult = getReplica(false).find(filter).first();
					String prevSharedList = queryResult.getString("shared");
					Bson updateOperation = createUnshareOperation(prevSharedList, unsharedList);
					UpdateResult updateResult;

					try {
						updateResult = getReplica(false).updateOne(queryResult, updateOperation);
					} catch (Exception e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}

					System.out.println(updateResult);
				}
			}
		}
	}

	public String deleteFile(ArrayList<String> files, String userName) {
		ArrayList<String> deletedFiles = new ArrayList<>();
		DeleteResult deleteResult;
		for (String fileName : files) {
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			try {
				deleteResult = getReplica(true).deleteOne(filter);
			} catch (Exception e) {
				recoverFromDatabaseFailure();
				deleteResult = getReplica(true).deleteOne(filter);
			}

			if (deleteResult.getDeletedCount() == 1){
				deletedFiles.add(fileName);
			}
		}

		return String.join(",", deletedFiles);
	}

	public void deleteFile(ArrayList<ArrayList<String>> files, String userName, boolean isReplicating) {
		if (isReplicating) {
			for (ArrayList<String> innerTSList : files) {
				String fileName = innerTSList.get(0);
				String key = String.join(",", userName, fileName);
				long entryTimestamp = Long.parseLong(innerTSList.get(1));
				long latestTimestamp = NetworkUtil.getTimestamp(key);

				if (entryTimestamp >= latestTimestamp) {
					Bson filter = createUsernameFilenameFilter(userName, fileName);
					try {
						getReplica(false).deleteOne(filter);
					} catch (Exception e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
				}
			}
		}
	}
}