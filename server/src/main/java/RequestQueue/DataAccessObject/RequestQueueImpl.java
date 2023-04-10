package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.Queue;

@Repository("requestQueue")

/**
 The RequestQueueImpl class implements the RequestQueue interface to create a request queue.
 It uses a LinkedList to store the request queue.
 */
public class RequestQueueImpl implements RequestQueue{
    private static final Queue<JsonNode> requestQueue = new LinkedList<>();;

    @Override
    public synchronized void produceMessage(JsonNode message) {
        if (message != null && !message.isEmpty()) {
            requestQueue.add(message);
        }
    }

    @Override
    public synchronized JsonNode consumeMessage() {
        return requestQueue.poll();
    }
}
