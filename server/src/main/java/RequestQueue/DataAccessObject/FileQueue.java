package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository("fileQueue")
public class FileQueue {
    HashMap<String, Integer> fq;
    HashMap<String, Integer> fqTail;

    public FileQueue(){
        fq = new HashMap<>();
        fqTail = new HashMap<>();
    }

    /**

     Returns the head value of the queue for the given key.
     @param key the key to retrieve the head value from
     @return the head value of the queue for the given key
     */
    public synchronized Integer getHead(String key){
        return fq.get(key);
    }


    /**
     Removes the head value of the queue for the given key and updates the queue with the next order value.
     @param key the key to remove the head value from
     */
    public synchronized void removeHead(String key){
        int nextOrder = (fq.containsKey(key) ? fq.get(key) : 0)+1;
        fq.put(key,nextOrder);
    }

    /**
     Adds the given file request to the tail of the queue and updates its order value and key value.
     @param request the file request to add to the queue
     */
    public synchronized void addTail(JsonNode request){
        String fileName = request.get("fileName").asText();
        String ownerName = request.get("userName").asText();
        String key = ownerName+":"+fileName;

        if(!fq.containsKey(key)) fq.put(key,0);
        if(!fqTail.containsKey(key)) fqTail.put(key,0);
        ((ObjectNode) request).put("orderValue",fqTail.get(key));
        ((ObjectNode) request).put("keyValue",key);
        fqTail.put(key,fqTail.get(key)+1);
    }
}
