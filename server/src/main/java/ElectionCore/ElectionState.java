package ElectionCore;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ElectionState {
    public InetAddress serverIP;
    public boolean isRunning;
    public InetAddress leaderIP;

    public ElectionState() throws UnknownHostException {
        this.isRunning = false;
        leaderIP = null;
        serverIP = InetAddress.getLocalHost();
    }


}
