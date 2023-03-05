package ResponseQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public interface ResponseQueue {

    void push(String uName, JsonNode response);

    JsonNode pop(String uName);

}
