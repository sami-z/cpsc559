package MainServer.ElectionCore;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@JsonDeserialize(using = ElectionStateDeserializer.class)
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

    public ElectionState(InetAddress leaderIP, InetAddress serverIP, boolean running) throws UnknownHostException {
        this.isRunning = running;
        this.leaderIP = leaderIP;
        this.serverIP = serverIP;
    }


}
