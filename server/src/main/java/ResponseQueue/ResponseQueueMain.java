package ResponseQueue;

public class ResponseQueueMain {



    public static void main(String[] args) {
        ResponseQueue rq = new ResponseQueue();
        ResponseQueueServer server = new ResponseQueueServer(1010,rq);
        Thread t1 = new Thread(server);
        t1.start();
        System.out.println("Server is running");
        System.out.print("curr");
        while(true);
    }

}
