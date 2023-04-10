package MainServer.ElectionCore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

@RestController
public class LeaderController {

    private HashMap<String, Queue<String>> csQ;
    private HashSet<String> mutex;

    /**

     Handles the request for a lock from a process for a particular resource name.
     @param processIP The IP address of the requesting process.
     @param resourceName The name of the resource the requesting process wants to lock.
     */
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

    /**

     Handles the HTTP POST request for releasing a lock on a resource.
     @param processIP The IP of the process that released the lock.
     @param resourceName The name of the resource for which the lock is being released.
     */
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
