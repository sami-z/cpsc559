package ResponseQueue.API;

import ResponseQueue.DeletionManager.DeletionRunner;
import ResponseQueue.Service.ResponseQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RequestMapping("/api/response")
@RestController
public class ResponseQueueController {
    private final ResponseQueueHandler responseQueueHandler;

    private HashSet<String> currRunners;

    @Autowired
    public ResponseQueueController(ResponseQueueHandler responseQueueHandler) {
        this.responseQueueHandler = responseQueueHandler;
        this.currRunners = new HashSet<>();
    }

    /**

     This method is a POST endpoint that receives a JSON request body
     It then extracts the "currentUser" field from the JSON object and uses it to push the response to the corresponding user's response queue.
     If the "currentUser" field is not present at the top level of the JSON object, it extracts it from the first object within the array and uses it instead.
     @param response the JSON request body
     */
    @PostMapping("/post")
    public void postRequest(@RequestBody JsonNode response) {
        String currUser = response.has("currentUser") ? response.get("currentUser").asText() : response.get(0).get("currentUser").asText();
        ((ObjectNode) response).put("deletionOrder",System.currentTimeMillis());
        if(response.has("currentUser"))
        {
            responseQueueHandler.push(response.get("currentUser").asText(), response);
        } else {
            responseQueueHandler.push(response.get(0).get("currentUser").asText(), response);
        }

        if(!currRunners.contains(currUser)) {
            System.out.println("starting runner");
            currRunners.add(currUser);
            new Thread(new DeletionRunner(currUser,responseQueueHandler)).start();
        }
    }
}