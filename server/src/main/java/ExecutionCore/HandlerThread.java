package ExecutionCore;

import Models.ClientRequestModel;
import Network.HTTPRequestParser;
import Network.HttpFormatException;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import main.java.DB;
import org.json.simple.parser.ParseException;


public class HandlerThread implements Runnable {

    private String HTTPString;
    private Socket clientSocket;
    private DB db;

    public HandlerThread(Socket clientSocket, String string){
        this.clientSocket = clientSocket;
    }

    public void run() {
        // Parse HTML

        StringBuilder socketData = new StringBuilder();
        try {
            InputStream is = clientSocket.getInputStream();
            int read;
            byte[] buffer = new byte[1024];
            while((read = is.read(buffer)) != -1){
                socketData.append(new String(buffer,0,read));
            }
        } catch (IOException e) {
            return;
        }


        HTTPString = new String(socketData);
        HTTPRequestParser parser = new HTTPRequestParser();
        try {
            parser.parseRequest(HTTPString);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpFormatException e) {
            e.printStackTrace();
        }

        System.out.println(HTTPString);

        Gson g = new Gson();
        ClientRequestModel request = g.fromJson(HTTPString, ClientRequestModel.class);



        // If contains file save file
        if(!request.bytes.isEmpty()) {
//            Path tempFile = Files.createTempFile(null, null);
//            Files.write(tempFile, request.bytes.getBytes(StandardCharsets.UTF_8));
            //
        }
        // Check type of request
        if(request.requestType.toUpperCase().equals("READ")){ // locking
            try {
                db.findFiles(request.userName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            // Send to response queue
        }
        else if(request.requestType.toUpperCase().equals("WRITE")){ // locking
            try {
                db.uploadFile(request.fileName, request.userName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Send to response queue
        }
    }
}
