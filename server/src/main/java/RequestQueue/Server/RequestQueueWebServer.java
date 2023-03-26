package RequestQueue.Server;

import RequestQueue.DataAccessObject.FileQueue;
import RequestQueue.Leader.LeaderState;
import RequestQueue.Service.RequestQueueHandler;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.URL;
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

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(System.currentTimeMillis());
        System.out.println(s);

        JsonNode request = null;

        try {
            request = mapper.readTree(s);
        } catch (JsonProcessingException e) {
            System.out.println("Could not parse the JSON");
        }

        webSocket.close();

        if(!LeaderState.serverIP.equals(LeaderState.leaderIP) && request != null && !request.isEmpty()){
            try {
                NetworkUtil.sendWriteToLeader(LeaderState.leaderIP,request);
            } catch(RestClientException e){
                System.out.println("Could not send to leader for " + request.asText());
            }
        }
        if (request != null && !request.isEmpty()) {
            if(request.get("requestType").asText().equalsIgnoreCase("WRITE")) {
                fileQueue.increaseHead(request);
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
