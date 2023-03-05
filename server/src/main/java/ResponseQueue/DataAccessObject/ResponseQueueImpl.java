package ResponseQueue.DataAccessObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Repository;

@Repository("responseQueue")
public class ResponseQueueImpl implements ResponseQueue{

    private static final HashMap<String, Queue<JsonNode>> responseQueue = new HashMap<String, Queue<JsonNode>>();

    @Override
    public synchronized void push(String uName, JsonNode response){
        if(!responseQueue.containsKey(uName)){
            responseQueue.put(uName, new LinkedList<>());
        }

        responseQueue.get(uName).add(response);
    }

    @Override
    public synchronized JsonNode pop(String uName){
        if(!responseQueue.containsKey(uName) || responseQueue.get(uName).size() == 0) return null;
        return responseQueue.get(uName).poll();
    }

}
