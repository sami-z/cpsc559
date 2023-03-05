package ResponseQueue.Service;

import ResponseQueue.DataAccessObject.ResponseQueue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

@Service
public class ResponseQueueHandler{
    private ResponseQueue responseQueue;
    private ObjectMapper mapper;

    @Autowired
    public ResponseQueueHandler(@Qualifier("responseQueue") ResponseQueue responseQueue){
        this.responseQueue = responseQueue;
    }

    public void push(String uName, JsonNode response){
        responseQueue.push(uName, response);
    }

    public JsonNode pop(String uName){
        return responseQueue.pop(uName);
    }


//    @Override
//    public void run() {
//        mapper = new ObjectMapper();
//        JsonNode node = null;
//
//        try {
//            node = mapper.readTree(clientSocket.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(node.toPrettyString());
//
//        String uName = node.get("userName").asText();
//
//        rq.push(uName,node);
//
//        try {
//            clientSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}
