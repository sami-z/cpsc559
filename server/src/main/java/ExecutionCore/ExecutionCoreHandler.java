package ExecutionCore;

import Models.ClientRequestModel;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import DBServ.DB;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.client.RestTemplate;


public class ExecutionCoreHandler {

    private ObjectMapper mapper = new ObjectMapper();

    public static String readString(Socket clientSocket) throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        int availableBytes = inputStream.available();
        byte[] buffer = new byte[availableBytes];
        int bytesRead = 0;
        while (bytesRead < availableBytes) {
            int result = inputStream.read(buffer, bytesRead, availableBytes - bytesRead);
            if (result == -1) {
                break;
            }
            bytesRead += result;
        }

        System.out.println("END READING STRING");

        return new String(buffer);

    }

    public static void processEvent(JsonNode request, DB db) throws IOException {
        // Parse HTML

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//
//        Gson g = new Gson();
//        ClientRequestModel request = g.fromJson(rqMessage, ClientRequestModel.class);

//        Socket resQ = null;
//        try {
//            resQ = new Socket(NetworkConstants.RESPONSE_QUEUE_IP,NetworkConstants.RESPONSE_QUEUE_SERVER_PORT);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // If contains file save file


//        if(!request.bytes.isEmpty()) {
////            Path tempFile = Files.createTempFile(null, null);
////            Files.write(tempFile, request.bytes.getBytes(StandardCharsets.UTF_8));
//        }


        if (request == null) return;

        ArrayList<JSONObject> files = null;
        // Check type of request
        if(request.get("requestType").asText().equalsIgnoreCase("READ")){ // locking
            try {
                files = db.findFiles(request.get("userName").asText());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        else if(request.get("requestType").asText().equalsIgnoreCase("WRITE")){ // locking
            try {
                db.uploadFile(request.get("fileName").asText(), request.get("userName").asText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

//        DataOutputStream dout = new DataOutputStream(resQ.getOutputStream());
//        dout.write(files.get(0).toJSONString().getBytes(StandardCharsets.UTF_8));
//        dout.close();


    }
}
