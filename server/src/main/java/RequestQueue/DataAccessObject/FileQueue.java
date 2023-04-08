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

    public synchronized Integer getHead(String key){
        return fq.get(key);
    }

    public synchronized void removeHead(String key){
        int nextOrder = (fq.containsKey(key) ? fq.get(key) : 0)+1;
        fq.put(key,nextOrder);
    }

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
