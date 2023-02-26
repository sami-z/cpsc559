package ResponseQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;

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

        String uName = node.get("userName").asText();

        rq.push(uName,node);

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
