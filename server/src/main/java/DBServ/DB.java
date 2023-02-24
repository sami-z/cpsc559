package DBServ;

import static com.mongodb.client.model.Filters.eq;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
public class DB {
	private final static String URI = "mongodb+srv://cpsc559:cpsc559@cluster0.137nczk.mongodb.net/?retryWrites=true&w=majority";
	public MongoClient mongoClient;
	public MongoDatabase database;
	public MongoCollection<Document> filesCollection;
	
	DB() {
		this.mongoClient = MongoClients.create(URI);
        this.database = mongoClient.getDatabase("cpsc559_db");
        this.filesCollection = this.database.getCollection("files_data");
	}
	public void uploadFile(String filePath,String ownerName) throws IOException {
		
		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath));
    	String encodedString = Base64.getEncoder().encodeToString(fileContent);
        Document entry = new Document("_id", new ObjectId())
        	   .append("filename", "placeHolder.txt")
        	   .append("bytes", encodedString)
        	   .append("owner", ownerName)
        	   .append("created", "14/033/2023")
               .append("shared", Arrays.asList("ragya","sami"));
        filesCollection.insertOne(entry);
        System.out.println("Uploaded "+ filePath + " as " + ownerName);
	}
	public ArrayList<JSONObject> findFiles(String ownerName) throws ParseException {
		ArrayList<JSONObject> ret = new ArrayList<>();
		
		FindIterable<Document> doc = this.filesCollection.find(eq("owner",ownerName));
		if (doc != null) {
			System.out.println("Found files for " + ownerName + "!");
			for(Document d: doc) {
	        	JSONParser tempParser = new JSONParser();
	        	JSONObject tempJson = (JSONObject) tempParser.parse(d.toJson());
	        	ret.add(tempJson);
	        	System.out.println(">" + tempJson.get("filename"));
	        }
        } else {
            System.out.println("No match");
        }
		return ret;
	}
	public void saveFileFromDB(String filename, String dest) throws ParseException, IOException {
		Document doc = this.filesCollection.find(eq("filename", filename)).first();
		if (doc != null) {
			JSONParser parser = new JSONParser();
	    	JSONObject json = (JSONObject) parser.parse(doc.toJson());
	    	System.out.println("bytes: "+ json.get("bytes") + " end");
	    	byte[] fileBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary((String) json.get("bytes"));
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
    public static void main( String[] args ) throws IOException, ParseException {
        	DB myDB = new DB();
        	ArrayList<JSONObject> dbFiles = myDB.findFiles("manbir");
        	System.out.println(dbFiles.get(0).get("filename"));
        	String filePath = "C:\\Users\\rgmit\\OneDrive\\Desktop\\merge.txt";
			myDB.editSharedWith("merge.txt");

			//myDB.uploadFile(filePath);
        	//myDB.saveFileFromDB("merge.txt", "C:\\Users\\rgmit\\OneDrive\\Desktop\\ragMerge.txt");
     }
}