package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.Queue;

@Repository("requestQueue")
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
