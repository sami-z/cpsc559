package RequestQueue;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.Queue;

public class RequestQueue {

    private final Queue<JsonNode> requestQueue;

    public RequestQueue() {
        requestQueue = new LinkedList<>();
    }

    public synchronized void produceMessage(JsonNode message) {
        requestQueue.add(message);
    }

    public synchronized JsonNode consumeMessage() {
        if(requestQueue.size()>0) {
            return requestQueue.poll();
        }
        return null;
    }

}
