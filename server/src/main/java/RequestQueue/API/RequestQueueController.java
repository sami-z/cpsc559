package RequestQueue.API;

import DatabaseManager.DBManagerState;
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

    @GetMapping("/get-head/{key}")
    @ResponseBody
    public String getHead(@PathVariable String key){
        HeadItem hi = fileQueue.getHead(key);

        if(System.currentTimeMillis()-hi.currTime > 20 * 1000){
            fileQueue.removeHead(key);
        }

        return Integer.toString(hi.orderValue);
    }

    @GetMapping("/remove-head/{key}")
    @ResponseBody
    public void removeHead(@PathVariable String key){
        fileQueue.removeHead(key);
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

    @GetMapping("/get-leader")
    @ResponseBody
    public String getLeader(){
        System.out.println("TRYING TO GET LEADER");
        return LeaderState.leaderIP == null ? "" : LeaderState.leaderIP;
    }
}

