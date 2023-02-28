package ExecutionCore;

import Models.ClientRequestModel;
import Util.NetworkConstants;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import DBServ.DB;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;


public class ExecutionCoreHandler {

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

    public static void processEvent(Socket clientSocket, DB db) throws IOException {
        // Parse HTML



        String rqMessage = ExecutionCoreHandler.readString(clientSocket);

        System.out.println(rqMessage);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Gson g = new Gson();
        ClientRequestModel request = g.fromJson(rqMessage, ClientRequestModel.class);

        Socket resQ = null;
        try {
            resQ = new Socket(NetworkConstants.RESPONSE_QUEUE_IP,NetworkConstants.RESPONSE_QUEUE_SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If contains file save file


//        if(!request.bytes.isEmpty()) {
////            Path tempFile = Files.createTempFile(null, null);
////            Files.write(tempFile, request.bytes.getBytes(StandardCharsets.UTF_8));
//        }

        ArrayList<JSONObject> files = null;

        if (request == null) return;

        // Check type of request
        if(request.requestType.equalsIgnoreCase("READ")){ // locking
            try {
                files = db.findFiles(request.userName);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }
        else if(request.requestType.equalsIgnoreCase("WRITE")){ // locking
            try {
                db.uploadFile(request.fileName, request.userName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        DataOutputStream dout = new DataOutputStream(resQ.getOutputStream());
        dout.write(files.get(0).toJSONString().getBytes(StandardCharsets.UTF_8));
        dout.close();



    }
}
