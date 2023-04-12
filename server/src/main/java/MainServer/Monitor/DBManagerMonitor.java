package MainServer.Monitor;

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
    /**

     Sends an HTTP POST request to update the leader of the database manager and informs all the database manager servers  servers of the
     change in the leader.
     @param IP the IP address of the new leader of the request queue
     */
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


    /**
     This method sends a ping request to the database managers IPs to get the currently running database managers.
     It iterates through all the database managers IPs and sends a ping request to each of them. If a response is received from
     any of them, it returns that IP address, which is the currently running request queue. If none of the database managers
     responds to the ping request, the method returns null.
     @return The IP address of the currently running database managers or null if none of the request queues respond.
     */
    public static String getRunningDBManager(){

        for(String DBManagerIP : DB_MANAGER_IP){
            String ping_uri = NetworkConstants.getDBManagerPingURI(DBManagerIP);
            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
            try {
                restTemplate.getForEntity(ping_uri, String.class);
                System.out.println("PING SUCCEED");
                String DBManagerLeaderIP = restTemplate.getForEntity(get_leader_uri,String.class).getBody();

                if ( DBManagerLeaderIP.equals(EMPTY_DB_LEADER) || !NetworkUtil.validIP(DBManagerLeaderIP)){
                    NetworkUtil.notifyDBManagerLeader(DBManagerIP);
                    return DBManagerIP;
                }

                restTemplate.getForEntity(NetworkConstants.getDBManagerPingURI(DBManagerLeaderIP),String.class);
                return DBManagerLeaderIP;
            } catch(RestClientException e){
                System.out.println("DBManager did not respond to ping: " + DBManagerIP);
            }
        }
        return null;
    }

    /**
     * This code is used to check if the database manager leader is down.
     * If the database manager leader is down we elect a new leader and send this
     * information to all the other database manager
     * */
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
