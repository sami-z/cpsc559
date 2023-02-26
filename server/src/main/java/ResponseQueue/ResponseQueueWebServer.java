package ResponseQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ResponseQueueWebServer extends WebSocketServer {

    private ResponseQueue rq;
    private ObjectMapper mapper;

    public ResponseQueueWebServer(int port, ResponseQueue rq) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.rq = rq;
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
            JsonNode currRequest = mapper.readTree(s);
            String uName = currRequest.get("userName").asText();
            JsonNode currNode = rq.pop(uName);

            webSocket.send(currNode.asText().getBytes(StandardCharsets.UTF_8));
            webSocket.close();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
