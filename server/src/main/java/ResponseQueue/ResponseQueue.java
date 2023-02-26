package ResponseQueue;

import java.util.HashMap;
import java.util.Queue;

public class ResponseQueue {

    private HashMap<String, Queue<String>> rq;

    public ResponseQueue(){
        rq = new HashMap<String, Queue<>>();
    }

    public synchronized void push(String uName){
        if(rq.containsKey(uName)){
            
        }
    }

    public synchronized void pop(String uName){

    }

}
