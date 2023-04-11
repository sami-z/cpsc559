package ResponseQueue.DataAccessObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Repository;

@Repository("responseQueue")
public class ResponseQueueImpl implements ResponseQueue{

    private static final HashMap<String, Queue<JsonNode>> responseQueue = new HashMap<String, Queue<JsonNode>>();

    /**
     This method adds a given response to the queue of responses for a given user.
     If the queue does not already exist for the user, a new queue is created.
     This method is synchronized to ensure thread safety.
     @param uName The name of the user to whom the response is addressed.
     @param response The response to be added to the queue.
     */
    @Override
    public synchronized void push(String uName, JsonNode response){
        if(!responseQueue.containsKey(uName)){
            responseQueue.put(uName, new LinkedList<>());
        }
        responseQueue.get(uName).add(response);
    }

    public synchronized long getHeadTime(String uName){
        if(responseQueue.get(uName).size() == 0) return System.currentTimeMillis();
        return Long.parseLong(responseQueue.get(uName).peek().get("deletionOrder").asText());
    }

    /**
     Pop the response from the front of the response queue for a given username, and remove it from the queue.
     @param uName the username for which to retrieve the response
     @return the response JsonNode for the given username, or null if no response is available
     */
    @Override
    public synchronized JsonNode pop(String uName){
        if(!responseQueue.containsKey(uName) || responseQueue.get(uName).size() == 0) return null;
        return responseQueue.get(uName).poll();
    }

}
