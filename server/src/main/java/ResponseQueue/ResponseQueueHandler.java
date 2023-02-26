package ResponseQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ResponseQueueHandler implements Runnable{

    private Socket clientSocket;
    private ResponseQueue rq;
    private ObjectMapper mapper;

    public ResponseQueueHandler(Socket clientSocket, ResponseQueue rq){
        this.clientSocket = clientSocket;
        this.rq = rq;
    }

    @Override
    public void run() {
        mapper = new ObjectMapper();
        JsonNode node = null;

        try {
            node = mapper.readTree(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        String type = node.get("operationType").asText();
        String uName = node.get("userName").asText();

        if(type.toLowerCase(Locale.ROOT).equals("push")){
            rq.push(uName,node);
        }else{
            JsonNode response = rq.pop(uName);
            try {
                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                dout.write(response.asText().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}
