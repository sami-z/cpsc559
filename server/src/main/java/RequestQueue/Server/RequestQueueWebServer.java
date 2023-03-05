package RequestQueue.Server;

import RequestQueue.DataAccessObject.RequestQueue;
import RequestQueue.Service.RequestQueueHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RequestQueueWebServer extends WebSocketServer{
    private RequestQueueHandler requestQueueHandler;
    private ObjectMapper mapper;

    public RequestQueueWebServer(int portNumber, RequestQueueHandler requestQueueHandler) throws UnknownHostException {
        super(new InetSocketAddress(portNumber));
        this.requestQueueHandler = requestQueueHandler;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(s);
        try {
            JsonNode request = mapper.readTree(s);
            if (request != null && !request.isEmpty()) {
                requestQueueHandler.produceRequest(request);
            }
            webSocket.close();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
