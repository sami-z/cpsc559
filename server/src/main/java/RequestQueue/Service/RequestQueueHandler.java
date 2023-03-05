package RequestQueue.Service;

import RequestQueue.DataAccessObject.RequestQueue;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RequestQueueHandler{
    private final RequestQueue requestQueue;

    @Autowired
    public RequestQueueHandler(@Qualifier("requestQueue") RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public JsonNode fetchRequest() {
        return requestQueue.consumeMessage();
    }

    public void produceRequest(JsonNode request) {
        requestQueue.produceMessage(request);
    }

//    private void closeClientSocket(Socket clientSocket) {
//        try {
//            clientSocket.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void waitForPing() throws IOException {
//        DataInputStream In = new DataInputStream(clientSocket.getInputStream());
//        while(In.available() == 0);
//
//        byte[] b = new byte[In.available()];
//        In.read(b);
//    }

//        try {
//            while (true) {
//                waitForPing();
//                JsonNode response = requestQueue.consumeMessage();
//                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
//
//                if(response != null)
//                    dout.write(response.toPrettyString().getBytes(StandardCharsets.UTF_8));
//                else
//                    dout.write(NetworkConstants.EMPTY_QUEUE.getBytes(StandardCharsets.UTF_8));
//
//                System.out.println("RESPONSE SENT");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
}
