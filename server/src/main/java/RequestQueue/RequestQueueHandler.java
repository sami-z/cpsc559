package RequestQueue;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import Util.NetworkConstants;

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

    private void waitForPing(){

    }

    @Override
    public void run() {
        try {
            while(true) {
                waitForPing();
                JsonNode response = requestQueue.consumeMessage();
                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());

                if(response != null)
                    dout.write(response.asText().getBytes(StandardCharsets.UTF_8));
                else
                    dout.write(NetworkConstants.EMPTY_QUEUE.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
