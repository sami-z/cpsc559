package MainServer.ElectionCore;

import MainServer.Monitor.RequestQueueMonitor;
import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static Util.NetworkConstants.REQUEST_QUEUE_IPS;

public class ElectionConsumer {
    public static ObjectMapper mapper = new ObjectMapper();
    public static RestTemplate restTemplate = new RestTemplate();
    public static boolean response = false;
    public static boolean isBullied = false;

    public static void sendLeader(String IP){
        String uri = NetworkConstants.getProcessingServerURILeaderServer(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode rqNode = mapper.createObjectNode();
        ((ObjectNode)rqNode).put("leaderIP", ServerState.serverIP);
        HttpEntity<String> msg =
                new HttpEntity<String>(rqNode.toString(), headers);

        restTemplate.postForEntity(uri, msg, String.class);
    }

    public static void sendElection(String IP){
        String uri = NetworkConstants.getProcessingServerURIElection(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode rqNode = mapper.createObjectNode();
        ((ObjectNode)rqNode).put("leaderIP", IP);
        HttpEntity<String> msg =
                new HttpEntity<String>(rqNode.toString(), headers);

        restTemplate.postForEntity(uri, msg, String.class);
    }

    public static void initiateElection() throws UnknownHostException, InterruptedException {
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

    public static void setLeader(){
        for (String IP : NetworkConstants.SERVER_IPS){
            sendLeader(IP);
        }
        new Thread(new RequestQueueMonitor()).start();
    }

}
