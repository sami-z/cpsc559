package RequestQueue.API;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import RequestQueue.Thread.DeadlockRunner;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@RequestMapping("/api/request")
@RestController
public class RequestQueueController {
    private final RequestQueueHandler requestQueueHandler;
    private final FileQueue fileQueue;

    private final Set<String> deadlockRunnerMap;

    @Autowired
    public RequestQueueController(RequestQueueHandler requestQueueHandler, FileQueue fileQueue) {
        this.requestQueueHandler = requestQueueHandler;
        this.fileQueue = fileQueue;
        this.deadlockRunnerMap = new HashSet<>();
    }

    @GetMapping("/fetch")
    public JsonNode getRequest() {
        return requestQueueHandler.fetchRequest();
    }

    @GetMapping("/ping")
    public void ping(){
        return;
    }

    @GetMapping("/get-head/{key}/{order}")
    @ResponseBody
    public String getHead(@PathVariable String key, @PathVariable int order){
        int currOrder = fileQueue.getHead(key);
        String dlrKey = key + ":"+currOrder;

        if(!deadlockRunnerMap.contains(dlrKey)){
            deadlockRunnerMap.add(dlrKey);
            new Thread(new DeadlockRunner(currOrder,key,this.fileQueue)).start();
        }
        return Integer.toString(fileQueue.getHead(key));
    }

    @GetMapping("/remove-head/{key}/{order}")
    @ResponseBody
    public void removeHead(@PathVariable String key, @PathVariable int order){
        int currOrder = fileQueue.getHead(key);
        String dlrKey = key + ":"+currOrder;
        if(deadlockRunnerMap.contains(dlrKey)) deadlockRunnerMap.remove(dlrKey);
        if(order<currOrder) return;
        fileQueue.removeHead(key);
    }

    @PostMapping("/push")
    public void pushRequest(@RequestBody JsonNode node){
        if(node.get("requestType").asText().equalsIgnoreCase("WRITE")) {
            fileQueue.addTail(node);
        }
        requestQueueHandler.produceRequest(node);
    }


    @PostMapping("/push-test")
    public void pushRequestTest(@RequestBody JsonNode node){
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

