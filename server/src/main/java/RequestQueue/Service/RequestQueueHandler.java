package RequestQueue.Service;

import RequestQueue.DataAccessObject.RequestQueue;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RequestQueueHandler{
    private final RequestQueue requestQueue;

    public RequestQueueHandler(@Qualifier("requestQueue") RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public JsonNode fetchRequest() {
        return requestQueue.consumeMessage();
    }

    public void produceRequest(JsonNode request) {
        requestQueue.produceMessage(request);
    }
}
