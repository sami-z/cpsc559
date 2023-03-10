package RequestQueue.API;

import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return requestQueueHandler.fetchRequest();
    }

    @GetMapping("/ping")
    public void ping(){
        return;
    }

    @PostMapping("/push")
    public void pushRequest(@RequestBody JsonNode node){
        requestQueueHandler.produceRequest(node);
    }

    @PostMapping("/leader")
    public void setLeader(@RequestParam String leaderIP){
        LeaderState.leaderIP = leaderIP;
    }
}

