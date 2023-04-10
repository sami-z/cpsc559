package MainServer.ElectionCore;

import MainServer.ServerState;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@RestController
public class ElectionController{

    /**

     Handles the POST request to update the leader server.
     @param node the JSON data received in the request body containing the new leader's IP address
     @return void
     */
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

    /**

     This method handles HTTP POST requests to the "/election" endpoint.
     It receives a JSON object from a requesting server containing the IP address of another server.
     The method checks if the requesting server has a higher IP address than this server. If it does,
     and if there is no ongoing election, it triggers an election by calling the initiateElection() method of
     the ElectionConsumer class.
     @param node a JsonNode object representing the JSON data in the request body
     @throws InterruptedException if the thread is interrupted while sleeping
     @throws UnknownHostException if the IP address of this server or the requesting server cannot be resolved
     */
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

    /**

     Handles the POST request to set the new request queue leader IP address.
     The method receives a JsonNode object containing the new leader IP and sets it to ServerState.
     @param node the JsonNode containing the new leader IP.
     */
    @PostMapping("leader/requestqueue")
    public void requestLeader(@RequestBody JsonNode node){
        String requestQueueIP = node.get("requestQueueIP").asText();

        System.out.println("new Request queue leader" + requestQueueIP);

        ServerState.requestQueueIP = requestQueueIP;
    }

}
