package RequestQueue.DataAccessObject;

import java.util.HashMap;
import java.util.Queue;

public class FileQueue {
    HashMap<String, Integer> fq;

    public FileQueue(){
        fq = new HashMap<>();
    }

    public synchronized int getHead(String fileName){
        return fq.get(fileName);
    }

    public synchronized void removeHead(String fileName){
        fq.put(fileName,fq.getOrDefault(fileName,0)+1);
        return;
    }
}
