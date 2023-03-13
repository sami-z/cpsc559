package ResponseQueue.API;

import ResponseQueue.Service.ResponseQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/response")
@RestController
public class ResponseQueueController {
    private final ResponseQueueHandler responseQueueHandler;

    @Autowired
    public ResponseQueueController(ResponseQueueHandler responseQueueHandler) {
        this.responseQueueHandler = responseQueueHandler;
    }
    
    @PostMapping("/post")
    public void postRequest(@RequestBody JsonNode response) {
        System.out.println(response.toPrettyString());
        if(response.has("userName"))
        {
            responseQueueHandler.push(response.get("userName").asText(), response);
        } else {
            responseQueueHandler.push(response.get(0).get("userName").asText(), response);
        }


    }
}