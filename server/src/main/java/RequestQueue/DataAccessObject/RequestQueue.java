package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedList;
import java.util.Queue;

public interface RequestQueue {
    void produceMessage(JsonNode message);

    JsonNode consumeMessage();
}
