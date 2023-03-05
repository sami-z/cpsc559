package MainServer.ReplicaManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

@RestController
public class LeaderController {

    @Autowired
    private HashMap<String, Queue<String>> csQ;
    private HashSet<String> mutex;

    @PostMapping("/requestLock")
    public void requestLock(@RequestParam String processIP, @RequestParam String resourceName) {
        if(mutex.contains(resourceName)){
            csQ.putIfAbsent(resourceName,new LinkedList<>());
            csQ.get(resourceName).add(processIP);
        }else {
            mutex.add(resourceName);
            // send acquire to process
        }
    }

    @PostMapping("/releaseLock")
    @ResponseStatus(value = HttpStatus.OK)
    public void releaseLock(@RequestParam String processIP, @RequestParam String resourceName) {
        if(csQ.containsKey(resourceName) && csQ.get(resourceName).isEmpty()){
            mutex.remove(resourceName);
        }else{
            String newProcessIP = csQ.get(resourceName).poll();
            // send acquire
        }
    }
}
