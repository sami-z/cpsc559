package MainServer.Monitor;

import DatabaseManager.DBManagerState;
import DatabaseManager.DatabaseClusterMonitor;
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

import java.time.Duration;

import static Util.NetworkConstants.*;

public class DBManagerMonitor implements Runnable{

    public static ObjectMapper mapper = new ObjectMapper();

    static RestTemplate restTemplate;

    static {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
    }

    public static void sendDBManagerLeader(String IP){
        ServerState.DBManagerIP = IP;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode rqNode = mapper.createObjectNode();
        ((ObjectNode)rqNode).put("leaderIP", IP);
        HttpEntity<String> rqUpdate =
                new HttpEntity<String>(rqNode.toString(), headers);

        for (String DBManagerIP: DB_MANAGER_IP) {
            String DB_Manager_URI = NetworkConstants.setDBManagerLeaderURI(DBManagerIP);
            try {
                restTemplate.postForEntity(DB_Manager_URI, rqUpdate, String.class);
            } catch(RestClientException e){
                System.out.println("Could not update leader for " + DB_Manager_URI);
            }
        }
    }


    public static String getRunningDBManager(){

        for(String DBManagerIP : DB_MANAGER_IP){
            String ping_uri = NetworkConstants.getDBManagerPingURI(DBManagerIP);
            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
            try {
                restTemplate.getForEntity(ping_uri, String.class);
                System.out.println("PING SUCCEED");
                String DBManagerLeaderIP = restTemplate.getForEntity(get_leader_uri,String.class).getBody();

                if ( DBManagerLeaderIP.equals(EMPTY_DB_LEADER)){
                    NetworkUtil.notifyDBManagerLeader(DBManagerIP);
                    return DBManagerIP;
                }

                return DBManagerLeaderIP;
            } catch(RestClientException e){
                System.out.println("DBManager did not respond to ping: " + DBManagerIP);
            }
        }
        return null;
    }
    @Override
    public void run() {

        if(ServerState.DBManagerIP.isEmpty()){
            String newLeader = getRunningDBManager();
            System.out.println("New DB LEADER IS: 1" + newLeader);
            sendDBManagerLeader(newLeader);
        }

        while(ServerState.serverIP.equals(ServerState.leaderIP)){
            String ping_uri = NetworkConstants.getDBManagerPingURI(ServerState.DBManagerIP);
            try{
                restTemplate.getForEntity(ping_uri,String.class);
            }catch (RestClientException e){
                String newLeader = getRunningDBManager();
                System.out.println("New DB LEADER IS: 2" + newLeader);
                sendDBManagerLeader(newLeader);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
