package ResponseQueue;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ResponseQueue {

    private HashMap<String, Queue<JsonNode>> rq;

    public ResponseQueue(){
        rq = new HashMap<String, Queue<JsonNode>>();
    }

    public synchronized void push(String uName, JsonNode response){
        if(!rq.containsKey(uName)){
            rq.put(uName, new LinkedList<>());
        }

        rq.get(uName).add(response);
    }

    public synchronized JsonNode pop(String uName){
        if(!rq.containsKey(uName) || rq.get(uName).size() == 0) return null;
        return rq.get(uName).poll();
    }

}
