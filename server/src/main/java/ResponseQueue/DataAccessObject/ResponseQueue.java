package ResponseQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;

public interface ResponseQueue {

    void push(String uName, JsonNode response);

    JsonNode pop(String uName);

    long getHeadTime(String uName);

}
