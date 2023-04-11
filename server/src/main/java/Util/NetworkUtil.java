package Util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

import static Util.NetworkConstants.DB_MANAGER_IP;
import static Util.NetworkConstants.EMPTY_DB_LEADER;

public class NetworkUtil {

    /**

     Determines whether the first InetAddress object is greater than the second one.

     @param ip1 the first InetAddress object to be compared

     @param ip2 the second InetAddress object to be compared

     @return true if the first InetAddress object is greater than the second one, false otherwise

     @throws NullPointerException if either of the input InetAddress objects is null
     */
    public static boolean isGreater(InetAddress ip1, InetAddress ip2){
        byte[] b1 = ip1.getAddress();
        byte[] b2 = ip2.getAddress();

        for(int i = 0;i<b1.length;i++){
            if(b1[i]<b2[i]) return false;
            else if(b1[i]>b2[i]) return true;
        }

        return false;
    }

    /**

     Sends a JSON request to the specified response queue via REST API.

     @param rq the JSON request to be sent

     @param IP the IP address of the response queue

     @throws RestClientException if there is an error while sending the request
     */
    public static void sendToResponseQueue(JsonNode rq, String IP){
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.setConnectTimeout(Duration.ofMillis(1000)).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getResponseQueueURI(IP);

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        try {
            restTemplate.postForEntity(uri, request, String.class);
        } catch(RestClientException e){
        }
    }

    /**

     Retrieves the current leader of the specified request queue via REST API.
     @param IP the IP address of the request queue
     @return The string representing the current leader of the request queue (IP)
     @throws RestClientException if there is an error while retrieving the leader
     */
    public static String getRequestQueueLeader(String IP){
        RestTemplate restTemplate = new RestTemplate();
        String getHeadURI = NetworkConstants.getRequestQueueLeaderStateURI(IP);
        ResponseEntity<String> currOrder = restTemplate.getForEntity(getHeadURI,String.class);
        return currOrder.getBody();
    }


    /**
     Sends out the primary databse replica to all the database managers
     @param rq the JSON request to be broadcasted
     */
    public static void broadcastPrimaryReplica(JsonNode rq){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);
        for (String DBManagerIP : DB_MANAGER_IP) {
            String uri = NetworkConstants.getDBManagerBroadcastPrimaryURI(DBManagerIP);
            try {
                restTemplate.postForEntity(uri, request, String.class);
            } catch (RestClientException e){}
        }
    }

    /**

     Notifies the specified database manager that it is the new leader
     @param leaderIP the IP address of the new leader
     @throws RestClientException if there is an error while notifying the database manager
     */
    public static void notifyDBManagerLeader(String leaderIP){
        RestTemplate restTemplate = new RestTemplate();
        String notifyURI = NetworkConstants.notifyDBManagerLeaderURI(leaderIP);
        restTemplate.getForEntity(notifyURI,String.class);
    }

    /**

     Sends a write request to any of the available database managers via REST API.
     @param rq the JSON write request to be sent
     @return true if the document was replaced and false otherwise
     */
    public static boolean sendWrite(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                return sendWrite(rq,IP);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**

     Sends a write request to the specified DBManager IP address.
     @param rq The JSON node containing the write request.
     @param IP The IP address of the DBManager to send the write request to.
     @return True if the document was replaced and false otherwise
     */
    private static boolean sendWrite(JsonNode rq, String IP){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getDBManagerURI(IP);

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        ResponseEntity<Boolean> wasReplaced = restTemplate.postForEntity(uri,request,Boolean.class);
        return wasReplaced.getBody();
    }

    /**

     Sends a delete request to any of the available database managers via REST API.
     @param rq the JSON write request to be sent
     @return returns a list of deleted files
     */
    public static String sendDelete(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                return sendDelete(rq,IP);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**

     Sends a delete request to the specified DBManager IP address.
     @param rq The JSON node containing the write request.
     @param IP The IP address of the DBManager to send the write request to.
     @return returns a list of deleted files
     */
    private static String sendDelete(JsonNode rq, String IP){
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerDeleteURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> deleteRq = new HttpEntity<String>(rq.toString(), headers);
        ResponseEntity<String> deleteList = rt.postForEntity(URI, deleteRq, String.class);
        return deleteList.getBody();
    }

    /**

     Sends a share request to any of the available database managers via REST API.
     @param rq the JSON write request to be sent
     */
    public static void sendShare(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                sendShare(rq,IP);
                System.out.println("AAAA");
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return;
    }


    /**

     Sends a share request to the specified DBManager IP address.
     @param rq The JSON node containing the write request.
     @param IP The IP address of the DBManager to send the write request to.
     */
    private static void sendShare(JsonNode rq, String IP) {
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerShareURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> shareRq = new HttpEntity<String>(rq.toString(), headers);
        rt.postForEntity(URI, shareRq, String.class);
    }

    /**

     Sends a unshare request to any of the available database managers via REST API.
     @param rq the JSON write request to be sent
     */
    public static void sendUnShare(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                sendUnshare(rq,IP);
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return;
    }


    /**

     Sends a unshare request to the specified DBManager IP address.
     @param rq The JSON node containing the write request.
     @param IP The IP address of the DBManager to send the write request to.
     */
    private static void sendUnshare(JsonNode rq, String IP) {
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerUnShareURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> unshareRq = new HttpEntity<String>(rq.toString(), headers);
        rt.postForEntity(URI, unshareRq, String.class);
    }

    /**

     Sends a register request to any of the available database managers via REST API.
     @param rq the JSON write request to be sent
     */
    public static boolean sendRegister(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                return sendRegister(rq,IP);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /**

     Sends a register request to the specified DBManager IP address.
     @param rq The JSON node containing the write request.
     @param IP The IP address of the DBManager to send the write request to.
     */
    public static boolean sendRegister(JsonNode rq, String IP){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getDBManagerRegisterURI(IP);

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        ResponseEntity<Boolean> wasSuccessful = restTemplate.postForEntity(uri,request, Boolean.class);
        return wasSuccessful.getBody();
    }

    /**

     Sends a write request to the leader DBManager with the specified IP address.
     @param IP The IP address of the leader DBManager to send the write request to.
     @param request The JSON node containing the write request.
     */
    public static void sendWriteToLeader(String IP, JsonNode request){
        RestTemplate rt = new RestTemplate();
        String request_queue_uri = NetworkConstants.getRequestQueuePushURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> rqUpdate = new HttpEntity<String>(request.toString(), headers);
        rt.postForEntity(request_queue_uri, rqUpdate, String.class);
    }

    /**

     Retrieves the timestamp of the head of the version list for the specified file from the DB Manager's server at the local host IP.
     @param fileName the name of the file whose timestamp should be retrieved.
     @return the timestamp of the head of the version list for the specified file.
     @throws RuntimeException if the local host IP address cannot be determined.
     */
    public static long getTimestamp(String fileName) {
        RestTemplate restTemplate = new RestTemplate();
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String ip = address.getHostAddress();
        String getHeadURI = NetworkConstants.getDBManagerGetHeadURI(ip, fileName);
        ResponseEntity<Long> timestamp = restTemplate.getForEntity(getHeadURI, Long.class);
        return timestamp.getBody();
    }

    /**

     This method is used to retrieve the current head order of a specific key in the request queue from a specific IP address.
     @param IP The IP address of the server that stores the request queue.
     @param key The specific key of the request queue.
     @param order The order of the request queue to retrieve.
     @return The current head order of the specific key in the request queue.
     */
    public static int getRequestHead(String IP, String key, int order){
        RestTemplate restTemplate = new RestTemplate();
        String getHeadURI = NetworkConstants.getRequestQueueHeadURI(IP,key,order);
        ResponseEntity<Integer> currOrder = restTemplate.getForEntity(getHeadURI,Integer.class);
        return currOrder.getBody();
    }

    /**

     Sends a GET request to release the lock for the given key and order in the request queue of the specified IP address.
     @param IP the IP address of the request queue server
     @param key the key to identify the request queue
     @param order the order of the request in the queue
     */
    public static void releaseLock(String IP, String key, int order){
        RestTemplate restTemplate = new RestTemplate();
        String removeHeadURI = NetworkConstants.getRequestQueueRemoveHeadURI(IP,key,order);
        restTemplate.getForEntity(removeHeadURI,String.class);
    }

    public static void callReplicaRecovery() {
        RestTemplate restTemplate = new RestTemplate();

        for (String DBManagerIP : DB_MANAGER_IP) {
            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
            String DBManagerLeaderIP = restTemplate.getForEntity(get_leader_uri,String.class).getBody();

            if (!DBManagerLeaderIP.equals(EMPTY_DB_LEADER)) {
                String notifyURI = NetworkConstants.notifyDBManagerLeaderPrimaryDownURI(DBManagerLeaderIP);
                restTemplate.getForEntity(notifyURI,String.class);
                return;
            }
        }
    }

    public static void callPrimaryReplicaUp() {
        RestTemplate restTemplate = new RestTemplate();

        for (String DBManagerIP : DB_MANAGER_IP) {
            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
            String DBManagerLeaderIP = restTemplate.getForEntity(get_leader_uri,String.class).getBody();

            if (!DBManagerLeaderIP.equals(EMPTY_DB_LEADER)) {
                String notifyURI = NetworkConstants.notifyDBManagerLeaderPrimaryUpURI(DBManagerLeaderIP);
                restTemplate.getForEntity(notifyURI,String.class);
                return;
            }
        }
    }
}
