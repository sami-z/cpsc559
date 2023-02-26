package ResponseQueue;

import java.net.UnknownHostException;

public class ResponseQueueMain {



    public static void main(String[] args) throws UnknownHostException {
        ResponseQueue rq = new ResponseQueue();
        ResponseQueueServer server = new ResponseQueueServer(1010,rq);
        Thread t1 = new Thread(server);
        t1.start();
        System.out.println("Server is running");
        System.out.print("curr");

        ResponseQueueWebServer webServer = new ResponseQueueWebServer(80,rq);
        webServer.start();
        while(true);
    }

}
