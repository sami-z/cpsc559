package RequestQueue.Server;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.Leader.LeaderRunner;
import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import Util.NetworkUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.web.client.RestClientException;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RequestQueueWebServer extends WebSocketServer{
    private final RequestQueueHandler requestQueueHandler;
    private final ObjectMapper mapper;
    private final FileQueue fileQueue;


    public RequestQueueWebServer(int portNumber, RequestQueueHandler requestQueueHandler, FileQueue fq) throws UnknownHostException {
        super(new InetSocketAddress(portNumber));
        this.requestQueueHandler = requestQueueHandler;
        this.mapper = new ObjectMapper();
        this.fileQueue = fq;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("websocket opened: " + System.currentTimeMillis());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    /**
     This class implements the WebSocketListener's onMessage method to handle incoming messages. It extracts
     the request information from the incoming message and performs the following actions:
     (1) Parses the JSON request from the incoming message.
     (2) If the current server is not the leader, creates a new thread and passes the request to LeaderRunner to send to the leader.
     (3) If the request type is WRITE, adds the request to the file queue for syncrhonization.
     (4) Adds the file to the request queue
     */
    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(System.currentTimeMillis());

        JsonNode request = null;

        try {
            request = mapper.readTree(s);
        } catch (JsonProcessingException e) {
            System.out.println("Could not parse the JSON");
        }

        webSocket.close();

        if(!LeaderState.serverIP.equals(LeaderState.leaderIP) && request != null && !request.isEmpty()){
            new Thread(new LeaderRunner(request)).start();
        }
        else if (request != null && !request.isEmpty()) {
            if(request.get("requestType").asText().equalsIgnoreCase("WRITE")) {
                fileQueue.addTail(request);
            }
            requestQueueHandler.produceRequest(request);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {
        System.out.println("Request queue web server started");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
