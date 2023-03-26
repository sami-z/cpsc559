package ResponseQueue.Service;

import ResponseQueue.DataAccessObject.ResponseQueue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.Socket;

@Service
public class ResponseQueueHandler{
    private ResponseQueue responseQueue;

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
}
