package RequestQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RequestQueueHandler implements Runnable{
    private final Socket clientSocket;
    private final RequestQueue requestQueue;

    public RequestQueueHandler(Socket clientSocket, RequestQueue requestQueue) {
        this.clientSocket = clientSocket;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode message = null;

        try {
            message = mapper.readTree(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String requestType = null;
        if (message != null) {
            requestType = message.get("operationType").asText();
        }

        if (requestType != null && requestType.toLowerCase(Locale.ROOT).equals("push")) {
            requestQueue.produceMessage(message);
        } else {
            JsonNode response = requestQueue.consumeMessage();
            try {
                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                dout.write(response.asText().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
