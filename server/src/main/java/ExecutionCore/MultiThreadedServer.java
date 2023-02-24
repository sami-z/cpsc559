package ExecutionCore;

import java.io.IOException;
import java.net.ServerSocket;
        import java.net.Socket;

public class MultiThreadedServer implements Runnable{
    int serverPort = 8080;
    ServerSocket serverSocket = null;
    boolean isRunning = false;


    public MultiThreadedServer(int port) {
        this.serverPort = port;
    }

    public synchronized void stop() {
        this.isRunning = false;
    }

    private synchronized void openServerSocket() {
        try {
            serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        this.isRunning = true;
        this.openServerSocket();

        while(this.isRunning){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                if(this.isRunning) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            new Thread(
                    new HandlerThread(
                            clientSocket, "Multithreaded Server")
            ).start();
        }
        System.out.println("Server Stopped.") ;

        System.out.println("Closing server");
    }
}