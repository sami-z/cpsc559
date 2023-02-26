package ExecutionCore;

import Models.ClientRequestModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import DBServ.DB;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


public class ExecutionCoreHandler {

    public static String readString(Socket clientSocket) throws IOException {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }

        return out.toString();

    }

    public static void processEvent(Socket clientSocket, DB db) throws IOException {
        // Parse HTML



        String rqMessage = ExecutionCoreHandler.readString(clientSocket);

        Gson g = new Gson();
        ClientRequestModel request = g.fromJson(rqMessage, ClientRequestModel.class);

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

        }
        else if(request.requestType.toUpperCase().equals("WRITE")){ // locking
            try {
                db.uploadFile(request.fileName, request.userName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



    }
}
