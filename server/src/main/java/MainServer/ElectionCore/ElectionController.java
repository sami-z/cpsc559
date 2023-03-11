package MainServer.ElectionCore;

import MainServer.ServerState;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@RestController
public class ElectionController{

    @PostMapping("/leader/server")
    public void leader(@RequestBody JsonNode node) {
        String leaderIP = node.get("leaderIP").asText();
        System.out.println("New leader is " + leaderIP);
        ServerState.leaderIP = leaderIP;
        ServerState.isElectionRunning = false;
        ElectionConsumer.response = true;
    }

    @GetMapping("/leader/ping")
    public void ping(){
        return;
    }

    @PostMapping("/election")
    @ResponseStatus(value = HttpStatus.OK)
    public void election(@RequestBody JsonNode node) throws InterruptedException, UnknownHostException {

        String otherIP = node.get("otherIP").asText();

        if(NetworkUtil.isGreater(
                InetAddress.getByName(ServerState.serverIP),
                InetAddress.getByName(otherIP))
        ){

            if(!ServerState.isElectionRunning){
                ElectionConsumer.initiateElection();
            }
        }
    }

    @PostMapping("/bully")
    public void bully(@RequestBody String node) throws InterruptedException {
        ServerState.isElectionRunning = false;
        ElectionConsumer.isBullied = true;
        ElectionConsumer.response = true;
    }

    @PostMapping("leader/requestqueue")
    public void requestLeader(@RequestBody JsonNode node){
        String requestQueueIP = node.get("requestQueueIP").asText();
        ServerState.requestQueueIP = requestQueueIP;
    }

}
