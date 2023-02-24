package DBServ;

import ExecutionCore.MultiThreadedServer;

public class ServerMain {

    public static void main(String[] args) {
        MultiThreadedServer server = new MultiThreadedServer(80);
        Thread t1 = new Thread(server);
        t1.start();
        System.out.println("Server is running");
        System.out.print("curr");
        while(true);
    }
}
