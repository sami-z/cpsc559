package ResponseQueue.API;

import ResponseQueue.Service.ResponseQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/response")
@RestController
public class ResponseQueueController {
    private final ResponseQueueHandler responseQueueHandler;

    @Autowired
    public ResponseQueueController(ResponseQueueHandler responseQueueHandler) {
        this.responseQueueHandler = responseQueueHandler;
    }

    @GetMapping
    public void postRequest(@RequestBody String uName,@RequestBody JsonNode response) {
        responseQueueHandler.push(uName, response);
    }
}