package ResponseQueue;

import java.net.UnknownHostException;

import Util.NetworkConstants;
import Util.NetworkConstants.*;

public class ResponseQueueMain {



    public static void main(String[] args) throws UnknownHostException {
        ResponseQueue rq = new ResponseQueue();
        ResponseQueueServer server = new ResponseQueueServer(NetworkConstants.RESPONSE_QUEUE_SERVER_PORT,rq);
        Thread t1 = new Thread(server);
        t1.start();
        System.out.println("Server is running");
        System.out.print("curr");

        ResponseQueueWebServer webServer = new ResponseQueueWebServer(NetworkConstants.RESPONSE_QUEUE_SOCKET_PORT,rq);
        webServer.start();
        while(true);
    }

}
