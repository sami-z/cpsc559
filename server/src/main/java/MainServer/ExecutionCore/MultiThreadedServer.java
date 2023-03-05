package MainServer.ExecutionCore;

import DatabaseManager.DB;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiThreadedServer implements Runnable{
    ServerSocket serverSocket = null;
    boolean isRunning = false;

    public synchronized void stop() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        this.isRunning = true;
//        Socket rqSocket = null;
//        try {
//            rqSocket = new Socket(NetworkConstants.REQUEST_QUEUE_IP,NetworkConstants.REQUEST_SERVER_SOCKET_PORT);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        DB db = new DB();

        //new Thread(new ElectionController()).start();

        while(this.isRunning){
//            try {
//                OutputStream output = rqSocket.getOutputStream();
//                output.write(NetworkConstants.PING_VALUE);
//                System.out.println("aaa");
//                ExecutionCoreHandler.processEvent(rqSocket,db);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            RestTemplate restTemplate = new RestTemplate();
            String uri = "http://localhost:8080/api/request";
            JsonNode request = restTemplate.getForObject(uri, JsonNode.class);

            // change this to correct check to see if nothing was in queue
            if (request == null) continue;

            try {
                ExecutionCoreHandler.processEvent(request, db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
//
//        try {
//            rqSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("Server Stopped.") ;
        System.out.println("Closing server");
    }
}