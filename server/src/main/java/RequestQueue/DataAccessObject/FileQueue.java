package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository("fileQueue")
public class FileQueue {
    HashMap<String, HeadItem> fq;
    HashMap<String, HeadItem> fqTail;

    public FileQueue(){
        fq = new HashMap<>();
        fqTail = new HashMap<>();
    }

    public synchronized HeadItem getHead(String key){
        return fq.get(key);
    }

    public synchronized void removeHead(String key){
        int nextOrder = (fq.containsKey(key) ? fq.get(key).orderValue : 0)+1;
        fq.put(key,new HeadItem(nextOrder,System.currentTimeMillis()));
    }

    public synchronized void addTail(JsonNode request){
        String fileName = request.get("fileName").asText();
        String ownerName = request.get("ownerName").asText();
        String key = ownerName+":"+fileName;

        if(!fq.containsKey(key)) fq.put(key,new HeadItem(0,System.currentTimeMillis()));
        if(!fqTail.containsKey(key)) fqTail.put(key,new HeadItem(0,System.currentTimeMillis()));
        ((ObjectNode) request).put("orderValue",fqTail.get(key).orderValue);
        ((ObjectNode) request).put("keyValue",key);
        fqTail.put(key,new HeadItem(fqTail.get(key).orderValue+1,System.currentTimeMillis()));
    }
}
