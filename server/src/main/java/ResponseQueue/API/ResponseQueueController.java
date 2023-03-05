package ResponseQueue.API;

import ResponseQueue.Service.ResponseQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResponseQueueController {
    private final ResponseQueueHandler responseQueueHandler;

    @Autowired
    public ResponseQueueController(ResponseQueueHandler responseQueueHandler) {
        this.responseQueueHandler = responseQueueHandler;
    }

    @RequestMapping(value="/api/response",method = RequestMethod.POST)
    public void postRequest(@RequestBody JsonNode response) {
        System.out.println(response.toPrettyString());
        responseQueueHandler.push(response.get("uName").asText(), response.get("content"));
    }
}