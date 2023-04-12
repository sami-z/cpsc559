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
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.event.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DB {
	public static MongoClient mongoClient1 = null;
	public static MongoClient mongoClient2 = null;
	private ObjectMapper mapper;
	public static Boolean isFirstClusterPrimary = true;
	public static boolean shouldRecover = false;

	public DB() {
		if (DB.mongoClient1 == null) {
			DB.mongoClient1 = createMongoClient(true);
		}
		if (DB.mongoClient2 == null) {
			DB.mongoClient2 = createMongoClient(false);
		}
		this.mapper = new ObjectMapper();
	}

	/**
	 Creates a new MongoClient with specified settings.
	 @param shouldGetPrimary if true, the method will create a client to connect to the primary cluster and false if it shoulder connect to a secondary cluster.
	 @return a MongoClient object created with the specified settings.
	 */
	public static MongoClient createMongoClient(boolean shouldGetPrimary) {
		String URI;
		if (shouldGetPrimary) {
			URI = (DB.isFirstClusterPrimary) ? DBConstants.MONGO_URI_CLUSTER1 : DBConstants.MONGO_URI_CLUSTER2;
		} else {
			URI = (DB.isFirstClusterPrimary) ? DBConstants.MONGO_URI_CLUSTER2 : DBConstants.MONGO_URI_CLUSTER1;
		}

		MongoClientSettings clientSettings;

		if (URI.equals(DBConstants.MONGO_URI_CLUSTER1)) {
			clientSettings = MongoClientSettings.builder()
					.applyConnectionString(new ConnectionString(URI))
					.addCommandListener(new CommandListener() {
						@Override
						public void commandStarted(final CommandStartedEvent event) {
							// Handle command started event
						}

						@Override
						public void commandSucceeded(final CommandSucceededEvent event) {
							// Handle command succeeded event
						}

						@Override
						public void commandFailed(final CommandFailedEvent event) {
							if (event.getThrowable() instanceof MongoException && DB.isFirstClusterPrimary) {
								recoverFromDatabaseFailure();
							} else if (DB.isFirstClusterPrimary) {
								recoverFromDatabaseFailure();
							}
						}
					})
					.applyToServerSettings(builder -> builder.addServerListener(new ServerListener() {
						@Override
						public void serverClosed(ServerClosedEvent event) {
							if (DB.isFirstClusterPrimary) {
								recoverFromDatabaseFailure();
							}
						}
					}))
					.applyToServerSettings(builder -> builder.addServerMonitorListener(new ServerMonitorListener() {
						@Override
						public void serverHearbeatStarted(ServerHeartbeatStartedEvent event) {
							// handle server heartbeat started event
						}

						@Override
						public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent event) {
							// handle server heartbeat succeeded event
						}

						@Override
						public void serverHeartbeatFailed(ServerHeartbeatFailedEvent event) {
							if (DB.isFirstClusterPrimary) {
								recoverFromDatabaseFailure();
							}
						}
					}))
					.build();
		} else {
			clientSettings = MongoClientSettings.builder()
					.applyConnectionString(new ConnectionString(URI))
					.build();
		}

		return MongoClients.create(clientSettings);
	}

	/**

	 Closes the MongoClient instances associated with this DB instance.
	 If the MongoClient1 or MongoClient2 is not null, it will be closed.
	 */
	public static void closeMongoClients() {
		if (DB.mongoClient1 != null) {
			DB.mongoClient1.close();
		}

		if (DB.mongoClient2 != null) {
			DB.mongoClient2.close();
		}
	}

	/**
	 Retrieves the MongoClient instance corresponding to the specified cluster,
	 depending on whether the primary cluster should be used or not.
	 @param shouldGetPrimary a boolean indicating whether the primary cluster should be used
	 @return a MongoClient instance corresponding to the specified cluster
	 */
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

	/**

	 This method is responsible for replicating the primary database to the secondary database.
	 It first retrieves all the documents from the primary replica, and then drops the secondary replica.
	 After that, it iterates over each document in the primary replica and inserts it into the secondary replica.
	 Finally, it performs the same operations on the login replica.
	 */
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

	/**

	 Creates a new MongoDB Document for storing file upload information based on the given parameters.
	 @param model the client request model containing file upload information.
	 @param timestamp the timestamp for the file upload.
	 @param id the object ID for the file upload, or null if creating a new entry.
	 @param formattedDate the formatted date for the file upload.
	 @param userName the username of the owner of the file.
	 @param currentUser the username of the current user uploading the file.
	 @return a MongoDB Document object containing the file upload information.
	 */
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

	public static void recoverFromDatabaseFailure() {
		System.out.println("MongoDB Atlas Primary Cluster is down in DB");

		NetworkUtil.DBManagerNotifyPrimaryChange(false, true);
		NetworkUtil.processingServerNotifyPrimaryChange(false);
		while (DB.isFirstClusterPrimary) {
			System.out.println("waiting for broadcast to go thru");
		}
	}

	/**

	 Creates a MongoDB query document for retrieving file upload information based on the specified user and file name.
	 The resulting query will search for documents where the "userName" field matches the given user name, or the "shared" field
	 contains the given user name as a substring, and where the "fileName" field matches the given file name.
	 @param userName the name of the user who uploaded or has access to the file
	 @param fileName the name of the file to retrieve information for
	 @return a MongoDB query document that can be used to retrieve file upload information
	 */
	public Document createUploadQuery(String userName, String fileName) {
		return new Document("$or",
				Arrays.asList(
						new Document("userName", userName),
						new Document("shared", new Document("$regex", ".*" + userName + ".*"))
				))
				.append("fileName", fileName);
	}

	/**
	 *
	 * Uploads a file to the database and creates a new entry if there is no existing entry
	 * with the given username and filename, or replaces the existing entry if there is one.
	 * @param model the ClientRequestModel containing information about the upload request
	 * @param timestamp the timestamp of the upload request
	 * @param queryResult the result of the query to check for an existing entry
	 * @return an ArrayList containing the new entry and a Document indicating
	 * */
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
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				getReplica(true).deleteOne(deleteFilter);
			}
			entry = createEntry(model, timestamp, existingObjectId, formattedDate, queryResult.getString("userName"), queryResult.getString("currentUser"));
			wasReplaced = true;
		}

		try {
			getReplica(true).insertOne(entry);
		} catch (MongoException e) {
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

	/**

	 Uploads a file to the database, replacing an existing entry if the entry's timestamp is greater than or equal to the latest timestamp for the given file.
	 @param entry The Document containing the information about the file to be uploaded.
	 @throws IOException if an I/O error occurs.
	 */

	public void uploadFile(Document entry) throws IOException {
		String fileName = entry.getString("fileName");
		String userName = entry.getString("userName");
		long entryTimestamp = entry.getLong("timestamp");
		String key = String.join(",", userName, fileName);
		long latestTimestamp = NetworkUtil.getTimestamp(key);

		if (entryTimestamp >= latestTimestamp) {
			Document query = createUploadQuery(userName, fileName);

			Document queryResult;
			try {
				queryResult = getReplica(false).find(query).first();
			} catch (MongoException e) {
				System.out.println("Secondary cluster is currently down in DB");
				return;
			}
			if (queryResult == null) {
				try {
					getReplica(false).insertOne(entry);
				} catch (MongoException e) {
					System.out.println("Secondary cluster is currently down in DB");
					return;
				}
			} else {
				ObjectId existingObjectId = queryResult.getObjectId("_id");
				Bson deleteFilter = Filters.eq("_id", existingObjectId);
				try {
					getReplica(false).deleteOne(deleteFilter);
				} catch (MongoException e) {
					System.out.println("Secondary cluster is currently down in DB");
					return;
				}
				try {
					getReplica(false).insertOne(entry);
				} catch (MongoException e) {
					System.out.println("Secondary cluster is currently down in DB");
				}
			}
		}
	}

	/**

	 Registers a user in the system by inserting a new document into the login collection
	 if the user is not already registered.
	 @param model a ClientRequestModel object containing the current user's information
	 @return the newly created login document if registration is successful, otherwise null
	 */
	public Document registerUser(ClientRequestModel model) {
		Document query = new Document("userName", model.currentUser);
		Document queryResult;
		try {
			queryResult = getLoginReplica(true).find(query).first();
		} catch (MongoException e) {
			recoverFromDatabaseFailure();
			queryResult = getLoginReplica(true).find(query).first();
		}
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
		} catch (MongoException e) {
			recoverFromDatabaseFailure();
			getLoginReplica(true).insertOne(loginDoc);
		}

		return loginDoc;
	}


	/**

	 Inserts a new document into the login replica collection to register a new user.
	 @param entry a document that contains information about the user to be registered, including their username and password
	 @throws Exception if there is an error when attempting to insert the document into the login replica collection
	 */
	public void registerUser(Document entry) {
		try {
			getLoginReplica(false).insertOne(entry);
		} catch (MongoException e) {
			System.out.println("Secondary cluster is currently down in DB");
		}
	}

	/**

	 This method searches for files belonging to a given user, either files that the user has created or files that have been shared with the user.

	 @param userName the name of the user whose files are being searched for

	 @return an ArrayNode containing JsonNodes representing the matching documents in the database

	 @throws JsonProcessingException if there is an issue processing the JSON data
	 */
	public ArrayNode findFiles(String userName) throws JsonProcessingException {
		ArrayNode arrayNode = new ObjectMapper().createArrayNode();

		FindIterable<Document> docs;
		Document query = new Document("$or",
				Arrays.asList(
						new Document("userName", userName),
						new Document("currentUser", userName),
						new Document("shared", new Document("$regex", ".*" + userName + ".*"))
				));
		try {
			docs = getReplica(true).find(query);
		} catch (MongoException e) {
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

	/**

	 This method loads the specified file from the database and returns its content as a JsonNode.
	 @param userName the username of the owner of the file
	 @param fileName the name of the file to be loaded
	 @return the content of the file as a JsonNode, or null if the file is not found in the database
	 @throws IOException if there is an error reading the file content from the database
	 */
	public JsonNode loadFile(String userName, String fileName) throws IOException {
		Document doc;
		Bson filter = createUploadQuery(userName, fileName);
		try {
			doc = getReplica(true).find(filter).first();
		} catch (MongoException e) {
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

	/**
	 Creates a MongoDB update operation to modify the "shared" field of a document.
	 @param prevSharedList the current list of users the document is shared with, as a string separated by commas.
	 @param sharedList the new list of users to share the document with
	 @return a Bson update operation that can be passed to a MongoDB update method, such as updateOne or updateMany.
	 */
	public void editSharedWith(ArrayList<String> filesToShare, String userName, ArrayList<String> sharedList){
		for(String fileName: filesToShare)
		{
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			Document queryResult;
			try {
				queryResult = getReplica(true).find(filter).first();
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				queryResult = getReplica(true).find(filter).first();
			}
			String prevSharedList = queryResult.getString("shared");
			Bson updateOperation = createShareOperation(prevSharedList, sharedList);
			UpdateResult updateResult;

			try {
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				updateResult = getReplica(true).updateOne(queryResult, updateOperation);
			}

			System.out.println(updateResult);
		}
	}

	/**

	 Edits the list of users that a file is shared with in the database.
	 @param files An ArrayList of ArrayLists containing the name of the file and the timestamp of the last edit for each file that needs to be updated.
	 @param userName The username of the user who is making the edit.
	 @param sharedList An ArrayList of strings containing the usernames of the users that the file will now be shared with.
	 @param isReplicating A boolean indicating whether or not the database is in replication mode.
	 */
	public void editSharedWith(ArrayList<ArrayList<String>> files, String userName, ArrayList<String> sharedList, boolean isReplicating) {
		if (isReplicating) {
			for (ArrayList<String> innerTSList : files) {
				String fileName = innerTSList.get(0);
				String key = String.join(",", userName, fileName);
				long entryTimestamp = Long.parseLong(innerTSList.get(1));
				long latestTimestamp = NetworkUtil.getTimestamp(key);
				if (entryTimestamp >= latestTimestamp) {
					Bson filter = createUsernameFilenameFilter(userName, fileName);
					Document queryResult;
					try {
						queryResult = getReplica(false).find(filter).first();
					} catch (MongoException e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
					String prevSharedList = queryResult.getString("shared");
					Bson updateOperation = createShareOperation(prevSharedList, sharedList);

					try {
						getReplica(false).updateOne(queryResult, updateOperation);
					} catch (MongoException e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
				}
			}
		}
	}

	/**

	 This method creates a Bson update operation to remove the specified usernames from a shared list.
	 @param prevSharedList the previous shared list in string format
	 @param unsharedList the list of usernames to be removed from the shared list
	 @return a Bson update operation to be executed on a MongoDB collection
	 */
	public Bson createUnshareOperation(String prevSharedList, ArrayList<String> unsharedList) {

		String[] shared = prevSharedList.split("\\s*,\\s*");
		ArrayList<String> arrayList = new ArrayList<>();

		for (int i = 0; i < shared.length; i++) {
			arrayList.add(shared[i]);
		}

		arrayList.removeAll(unsharedList);
		if (arrayList.isEmpty()){
			return set("shared", "");
		}
		return set("shared", String.join(",", arrayList));
	}

	/**

	 Edits the shared list of the specified files to remove the given usernames.
	 @param filesToUnShare A list of filenames to edit the shared list for.
	 @param userName The username of the user who is unsharing the files.
	 @param unshareList A list of usernames to remove from the shared list of the specified files.
	 @throws IOException If an error occurs while communicating with the database.
	 */
	public void editUnsharedWith(ArrayList<String> filesToUnShare, String userName, ArrayList<String> unshareList){
		for(String fileName: filesToUnShare)
		{
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			Document queryResult;
			try {
				queryResult = getReplica(true).find(filter).first();
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				queryResult = getReplica(true).find(filter).first();
			}
			String prevSharedList = queryResult.getString("shared");
			Bson updateOperation = createUnshareOperation(prevSharedList, unshareList);

			try {
				getReplica(true).updateOne(queryResult, updateOperation);
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				getReplica(true).updateOne(queryResult, updateOperation);
			}
		}
	}

	/**

	 Edits the list of users with whom a file is unshared, for the given list of files, for the given user.
	 If isReplicating is true, the changes are made to the secondary cluster, otherwise to the primary cluster.
	 @param files the list of files to be unshared
	 @param userName the user for whom the files are to be unshared
	 @param unsharedList the list of users with whom the files are to be unshared
	 @param isReplicating a boolean flag indicating whether the changes should be made to the secondary cluster
	 */
	public void editUnsharedWith(ArrayList<ArrayList<String>> files, String userName, ArrayList<String> unsharedList, boolean isReplicating) {
		if (isReplicating) {
			for (ArrayList<String> innerTSList : files) {
				String fileName = innerTSList.get(0);
				String key = String.join(",", userName, fileName);
				long entryTimestamp = Long.parseLong(innerTSList.get(1));
				long latestTimestamp = NetworkUtil.getTimestamp(key);
				if (entryTimestamp >= latestTimestamp) {
					Bson filter = createUsernameFilenameFilter(userName, fileName);
					Document queryResult;
					try {
						queryResult = getReplica(false).find(filter).first();
					} catch (MongoException e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
					String prevSharedList = queryResult.getString("shared");
					Bson updateOperation = createUnshareOperation(prevSharedList, unsharedList);

					try {
						getReplica(false).updateOne(queryResult, updateOperation);
					} catch (MongoException e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
				}
			}
		}
	}

	/**

	 Deletes files with the given file names for the given user from the database.
	 @param files List of file names to be deleted
	 @param userName Username of the user whose files are to be deleted
	 @return String with names of deleted files separated by commas
	 */
	public String deleteFile(ArrayList<String> files, String userName) {
		ArrayList<String> deletedFiles = new ArrayList<>();
		DeleteResult deleteResult;
		for (String fileName : files) {
			Bson filter = createUsernameFilenameFilter(userName, fileName);
			try {
				deleteResult = getReplica(true).deleteOne(filter);
			} catch (MongoException e) {
				recoverFromDatabaseFailure();
				deleteResult = getReplica(true).deleteOne(filter);
			}

			if (deleteResult.getDeletedCount() == 1){
				deletedFiles.add(fileName);
			}
		}

		return String.join(",", deletedFiles);
	}

	/**

	 Deletes the specified files for a user.
	 If isReplicating is true, it will delete the files in both the primary and secondary clusters.
	 @param files the list of files to be deleted
	 @param userName the name of the user who owns the files
	 @param isReplicating specifies whether the deletion should be replicated across both the primary and secondary clusters
	 */
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
					} catch (MongoException e) {
						System.out.println("Secondary cluster is currently down in DB");
						return;
					}
				}
			}
		}
	}
}