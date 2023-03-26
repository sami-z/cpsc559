package RequestQueue.API;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/request")
@RestController
public class RequestQueueController {
    private final RequestQueueHandler requestQueueHandler;
    private final FileQueue fileQueue;

    @Autowired
    public RequestQueueController(RequestQueueHandler requestQueueHandler, FileQueue fileQueue) {
        this.requestQueueHandler = requestQueueHandler;
        this.fileQueue = fileQueue;
    }

    @GetMapping("/fetch")
    public JsonNode getRequest() {
        return requestQueueHandler.fetchRequest();
    }

    @GetMapping("/ping")
    public void ping(){
        return;
    }

    @GetMapping("/get-head/{filename}")
    @ResponseBody
    public String getHead(@PathVariable String filename){
        return Integer.toString(fileQueue.getHead(filename));
    }

    @GetMapping("/remove-head/{filename}")
    @ResponseBody
    public void removeHead(@PathVariable String filename){
        fileQueue.removeHead(filename);
    }

    @PostMapping("/push")
    public void pushRequest(@RequestBody JsonNode node){
        requestQueueHandler.produceRequest(node);
    }

    @PostMapping("/leader")
    public void setLeader(@RequestBody JsonNode node){
        String leaderIP = node.get("leaderIP").asText();
        LeaderState.leaderIP = leaderIP;
    }
}

