package MainServer.ElectionCore;

import MainServer.ServerState;
import Util.NetworkUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@RestController
public class ElectionController{

    @PostMapping("/leader/server")
    public void leader(@RequestParam String leaderIP) {
        ServerState.leaderIP = leaderIP;
    }

    @GetMapping("/leader/ping")
    public void ping(){
        return;
    }

    @PostMapping("/election")
    @ResponseStatus(value = HttpStatus.OK)
    public void election(@RequestParam String otherIP) throws InterruptedException, UnknownHostException {

        if(NetworkUtil.isGreater(InetAddress.getByName(ServerState.serverIP),
                InetAddress.getByName(otherIP))){

            if(!ServerState.isElectionRunning){
                ElectionConsumer.initiatieElection();
            }
        }
    }

    @PostMapping("/bully")
    public void bully(@RequestBody String node) throws InterruptedException {
        ServerState.isElectionRunning = false;
        ElectionConsumer.
    }

    @PostMapping("leader/requestqueue")
    public void requestLeader(@RequestAttribute String requestQueueIP){
        ServerState.requestQueueIP = requestQueueIP;
    }

}
