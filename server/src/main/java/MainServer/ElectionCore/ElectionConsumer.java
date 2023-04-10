package MainServer.ElectionCore;

import MainServer.Monitor.DBManagerMonitor;
import MainServer.Monitor.RequestQueueMonitor;
import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ElectionConsumer {
    public static ObjectMapper mapper = new ObjectMapper();
    public static RestTemplate restTemplate;
    public static boolean response = false;
    public static boolean isBullied = false;

    static{
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
    }

    /**

     Sends the leader IP to a processing server identified by the provided IP address.

     @param IP the IP address of the processing server to send the leader IP to

     @throws RestClientException if an error occurs during the REST API call
     */
    public static void sendLeader(String IP){

        try {
            String uri = NetworkConstants.getProcessingServerURILeaderServer(IP);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JsonNode rqNode = mapper.createObjectNode();
            ((ObjectNode) rqNode).put("leaderIP", ServerState.serverIP);
            HttpEntity<String> msg =
                    new HttpEntity<String>(rqNode.toString(), headers);
            restTemplate.postForEntity(uri, msg, String.class);
        } catch (RestClientException e){
            e.printStackTrace();
        }
    }

    /**

     Sends an election message to the specified IP address.

     @param IP the IP address to send the message to

     @throws RestClientException if there is an error while sending the message
     */
    public static void sendElection(String IP){
        try {
            String uri = NetworkConstants.getProcessingServerURIElection(IP);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JsonNode rqNode = mapper.createObjectNode();
            ((ObjectNode) rqNode).put("otherIP", ServerState.serverIP);
            HttpEntity<String> msg =
                    new HttpEntity<String>(rqNode.toString(), headers);

            restTemplate.postForEntity(uri, msg, String.class);
        } catch (RestClientException e){
            //e.printStackTrace();
            System.out.println("aaa");
        }
    }

    /**

     This method initiates the election process in case the current server detects that the leader is down. It sets the current server state to indicate that the election is running and then it checks which servers have IP addresses that are numerically greater than the current server's IP address. It sends an election message to all of the servers with IP addresses greater than its own. If no such servers exist, this server sets itself as the leader. Otherwise, it waits for a response from the servers to which it has sent the election message for a brief period of time. If it receives a positive response, it becomes a follower and waits for the new leader to send heartbeats. If it does not receive a response or is bullied by a server with a higher priority, it initiates a new election.
     @throws UnknownHostException if there is an error resolving the IP addresses of the servers.
     @throws InterruptedException if there is an error during the sleep operation.
     */
    public static void initiateElection() throws UnknownHostException, InterruptedException {

        System.out.println("STARTING THE ELECTION");

        ServerState.isElectionRunning = true;
        List<String> higher = new ArrayList<>();

        for(String ip : NetworkConstants.SERVER_IPS){
            InetAddress currIP = InetAddress.getByName(ip);
            if(NetworkUtil.isGreater(currIP, InetAddress.getByName(ServerState.serverIP))){
                higher.add(ip);
            }
        }

        if(higher.size() == 0){
            setLeader();
            return;
        }

        for(String IP : higher){
            sendElection(IP);
        }

        System.out.println("SEND ELECTION");

        ElectionConsumer.isBullied = false;
        ElectionConsumer.response = false;
        Thread.sleep(1000);
        if(!ElectionConsumer.response){
            ElectionConsumer.setLeader();
        }else if(ElectionConsumer.isBullied){
            ElectionConsumer.response = false;
        }

        System.out.println(ServerState.leaderIP);

    }

    /**

     Sets the current server as the leader and updates the leader IP across all servers in the network.
     Sends the request queue to the new leader for processing.
     Starts a new thread for monitoring the request queue and a new thread for monitoring the database manager.
     */
    public static void setLeader(){
        ServerState.leaderIP = ServerState.serverIP;

        if(ServerState.requestQueueIP.isEmpty()){
            ServerState.requestQueueIP = RequestQueueMonitor.getRunningRequestQueue();
        }

        RequestQueueMonitor.sendRequestQueueLeader(ServerState.requestQueueIP);

        for (String IP : NetworkConstants.SERVER_IPS){
            if(!IP.equals(ServerState.serverIP))
                sendLeader(IP);
        }
        new Thread(new RequestQueueMonitor()).start();
        new Thread(new DBManagerMonitor()).start();
    }

}
