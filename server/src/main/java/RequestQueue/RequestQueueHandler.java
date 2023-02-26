package RequestQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestQueueHandler implements Runnable{
    private final Socket clientSocket;
    private final RequestQueue requestQueue;

    public RequestQueueHandler(Socket clientSocket, RequestQueue requestQueue) {
        this.clientSocket = clientSocket;
        this.requestQueue = requestQueue;
    }

    private void closeClientSocket(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode response = requestQueue.consumeMessage();
        try {
            DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
            dout.write(response.asText().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        closeClientSocket(clientSocket);
    }
}
