package DBServ;

import ExecutionCore.ElectionController;
import ExecutionCore.MultiThreadedServer;
import ResponseQueue.ResponseQueueServer;
import org.springframework.boot.SpringApplication;

public class ServerMain {

    public static void main(String[] args) {
        SpringApplication.run(ElectionController.class,args);
        while(true);
//        MultiThreadedServer server = new MultiThreadedServer();
//        Thread t1 = new Thread(server);
//        t1.start();
//        System.out.println("Server is running");
//        System.out.print("curr");
//        while(true);
    }
}
