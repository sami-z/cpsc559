package ResponseQueue.Server;

import ResponseQueue.DataAccessObject.ResponseQueue;
import ResponseQueue.Service.ResponseQueueHandler;
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

    private ResponseQueueHandler responseQueueHandler;
    private ObjectMapper mapper;

    private String noUpdate = "";

    public ResponseQueueWebServer(int port, ResponseQueueHandler responseQueueHandler) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.responseQueueHandler = responseQueueHandler;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    /**
     This method is invoked when a new message is received on a WebSocket.
     It then extracts the current user's name from the message. It then retrieves the next response for the user from the responseQueueHandler.
     If no response is found for the user, it sends a "noUpdate" message on the WebSocket, otherwise it sends the retrieved response.
     @param webSocket the WebSocket connection object
     @param s the message received on the WebSocket
     */
    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(s);
        try {
            JsonNode currRequest = mapper.readTree(s);
            String uName = currRequest.get("currentUser").asText();

            JsonNode currNode = responseQueueHandler.pop(uName);

            if(currNode == null)
                webSocket.send(noUpdate.getBytes(StandardCharsets.UTF_8));

            else
                webSocket.send(currNode.toPrettyString().getBytes(StandardCharsets.UTF_8));

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
