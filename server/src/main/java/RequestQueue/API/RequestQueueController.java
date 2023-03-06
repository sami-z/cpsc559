package RequestQueue.API;

import RequestQueue.Service.RequestQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/request")
@RestController
public class RequestQueueController {
    private final RequestQueueHandler requestQueueHandler;

    @Autowired
    public RequestQueueController(RequestQueueHandler requestQueueHandler) {
        this.requestQueueHandler = requestQueueHandler;
    }

    @GetMapping("/fetch")
    public JsonNode getRequest() {
        System.out.println("REQUEST IS FETCHED");
        return requestQueueHandler.fetchRequest();
    }
}

