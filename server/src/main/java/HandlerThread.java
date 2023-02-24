import java.io.IOException;
import java.net.Socket;


public class HandlerThread implements Runnable {

    private String bytes;
    private Socket clientSocket;

    public HandlerThread(Socket clientSocket, String string){
        this.clientSocket = clientSocket;
    }

    public void run() throws IOException, HttpRequestParser.HttpFormatException {
        // Parse HTML
        clientSocket

        HttpRequestParser parser = new HttpRequestParser();
        parser.parseRequest(bytes);

        parser.
        // If contains file save file
        // Check type of request
        // If read then wait for file to be unlocked
            // read data
            // Send to response queue
        // If write then obtain lock on file
            // write data
            // Send to response queue
        // return
    }
}
