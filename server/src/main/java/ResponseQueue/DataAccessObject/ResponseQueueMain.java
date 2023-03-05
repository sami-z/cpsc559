package ResponseQueue.DataAccessObject;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import ResponseQueue.DataAccessObject.ResponseQueue;
import Util.NetworkConstants;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Repository;

@Repository("responseQueue")
public class ResponseQueueMain implements ResponseQueue{

    private static final HashMap<String, Queue<JsonNode>> responseQueue = new HashMap<String, Queue<JsonNode>>();

    @Override
    public synchronized void push(String uName, JsonNode response){
        if(!responseQueue.containsKey(uName)){
            responseQueue.put(uName, new LinkedList<>());
        }

        responseQueue.get(uName).add(response);
    }

    @Override
    public synchronized JsonNode pop(String uName){
        if(!responseQueue.containsKey(uName) || responseQueue.get(uName).size() == 0) return null;
        return responseQueue.get(uName).poll();
    }

//    public static void main(String[] args) throws UnknownHostException {
//        ResponseQueue rq = new ResponseQueue();
//        ResponseQueueServer server = new ResponseQueueServer(NetworkConstants.RESPONSE_QUEUE_SERVER_PORT,rq);
//        Thread t1 = new Thread(server);
//        t1.start();
//        System.out.println("Server is running");
//        System.out.print("curr");
//
//        ResponseQueueWebServer webServer = new ResponseQueueWebServer(NetworkConstants.RESPONSE_QUEUE_SOCKET_PORT,rq);
//        webServer.start();
//        while(true);
//    }

}
