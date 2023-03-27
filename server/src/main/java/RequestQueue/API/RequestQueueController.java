package RequestQueue.API;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.DataAccessObject.HeadItem;
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
        HeadItem hi = fileQueue.getHead(filename);

        if(System.currentTimeMillis()-hi.currTime > 20 * 1000){
            fileQueue.removeHead(filename);
        }

        return Integer.toString(hi.orderValue);
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

