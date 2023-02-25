package ExecutionCore;

import Kafka.ResponseQueueProducer;
import Models.ClientRequestModel;
import Network.HTTPRequestParser;
import Network.HttpFormatException;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import DBServ.DB;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


public class HandlerThread implements Runnable {

    private String messageString;
    private Socket clientSocket;
    private DB db;
    private ResponseQueueProducer producer;

    public HandlerThread(String message){
        this.messageString = message;
    }

    public void run() {
        // Parse HTML

        Gson g = new Gson();
        ClientRequestModel request = g.fromJson(messageString, ClientRequestModel.class);

        // If contains file save file
        if(!request.bytes.isEmpty()) {
//            Path tempFile = Files.createTempFile(null, null);
//            Files.write(tempFile, request.bytes.getBytes(StandardCharsets.UTF_8));
        }
        // Check type of request
        if(request.requestType.toUpperCase().equals("READ")){ // locking
            ArrayList<JSONObject> files;
            try {
                files = db.findFiles(request.userName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            for (JSONObject jsonObject : files) {
                String serializedJson = jsonObject.toString();
                producer.sendMessages(request.userName, serializedJson);
            }
        }
        else if(request.requestType.toUpperCase().equals("WRITE")){ // locking
            try {
                db.uploadFile(request.fileName, request.userName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            producer.sendMessages(request.userName,"SUCCESS");
        }
    }
}
