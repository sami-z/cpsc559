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

    /**

     This method is a POST endpoint that receives a JSON request body
     It then extracts the "currentUser" field from the JSON object and uses it to push the response to the corresponding user's response queue.
     If the "currentUser" field is not present at the top level of the JSON object, it extracts it from the first object within the array and uses it instead.
     @param response the JSON request body
     */
    @PostMapping("/post")
    public void postRequest(@RequestBody JsonNode response) {
        System.out.println(response.toPrettyString());
        if(response.has("currentUser"))
        {
            responseQueueHandler.push(response.get("currentUser").asText(), response);
        } else {
            responseQueueHandler.push(response.get(0).get("currentUser").asText(), response);
        }
    }
}