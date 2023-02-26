package RequestQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RequestQueueWebServer extends WebSocketServer{
    private RequestQueue requestQueue;
    private ObjectMapper mapper;

    public RequestQueueWebServer(int portNumber, RequestQueue requestQueue) throws UnknownHostException {
        super(new InetSocketAddress(portNumber));
        this.requestQueue = requestQueue;
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
        try {
            JsonNode request = mapper.readTree(s);
            if (request != null) {
                requestQueue.produceMessage(request);
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
