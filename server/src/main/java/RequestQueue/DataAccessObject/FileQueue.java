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

    public synchronized int getHead(String fileName){
        return fq.get(fileName);
    }

    public synchronized void removeHead(String fileName){
        fq.put(fileName,fq.getOrDefault(fileName,0)+1);
    }

    public synchronized void increaseHead(JsonNode request){
        String fileName = request.get("fileName").asText();

        if(!fq.containsKey(fileName)) fq.put(fileName,0);
        if(!fqTail.containsKey(fileName)) fqTail.put(fileName,0);
        ((ObjectNode) request).put("orderValue",fqTail.get(fileName));
        fqTail.put(fileName,fqTail.get(fileName)+1);
    }
}
