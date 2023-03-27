package RequestQueue.DataAccessObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;

@Repository("fileQueue")
public class FileQueue {
    HashMap<String, HeadItem> fq;
    HashMap<String, HeadItem> fqTail;

    public FileQueue(){
        fq = new HashMap<>();
        fqTail = new HashMap<>();
    }

    public synchronized HeadItem getHead(String fileName){
        return fq.get(fileName);
    }

    public synchronized void removeHead(String fileName){
        int nextOrder = (fq.containsKey(fileName) ? fq.get(fileName).orderValue : 0)+1;
        fq.put(fileName,new HeadItem(nextOrder,System.currentTimeMillis()));
    }

    public synchronized void increaseHead(JsonNode request){
        String fileName = request.get("fileName").asText();

        if(!fq.containsKey(fileName)) fq.put(fileName,new HeadItem(0,System.currentTimeMillis()));
        if(!fqTail.containsKey(fileName)) fqTail.put(fileName,new HeadItem(0,System.currentTimeMillis()));
        ((ObjectNode) request).put("orderValue",fqTail.get(fileName).orderValue);
        fqTail.put(fileName,new HeadItem(fqTail.get(fileName).orderValue+1,System.currentTimeMillis()));
    }
}
