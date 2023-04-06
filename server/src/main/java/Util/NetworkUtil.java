package Util;

import MainServer.ServerState;
import RequestQueue.Leader.LeaderState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class NetworkUtil {

    public static boolean isGreater(InetAddress ip1, InetAddress ip2){
        byte[] b1 = ip1.getAddress();
        byte[] b2 = ip2.getAddress();

        for(int i = 0;i<b1.length;i++){
            if(b1[i]<b2[i]) return false;
            else if(b1[i]>b2[i]) return true;
        }

        return false;
    }

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

    public static String getRequestQueueLeader(String IP){
        RestTemplate restTemplate = new RestTemplate();
        String getHeadURI = NetworkConstants.getRequestQueueLeaderStateURI(IP);
        ResponseEntity<String> currOrder = restTemplate.getForEntity(getHeadURI,String.class);
        return currOrder.getBody();
    }


    public static void broadcastPrimaryReplica(JsonNode rq, String leaderIP){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);
        for (String DBManagerIP : DB_MANAGER_IP) {
            if (!DBManagerIP.equals(leaderIP)) {
                String uri = NetworkConstants.getDBManagerBroadcastPrimaryURI(DBManagerIP);
                restTemplate.postForEntity(uri, request, String.class);
            }
        }
    }

//    public static void setIsFirstClusterPrimary(boolean newIsFirstPrimaryCluster) {
//        RestTemplate restTemplate = new RestTemplate();
//        String DBManagerLeaderIP = "";
//        for (String DBManagerIP : DB_MANAGER_IP){
//            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
//            DBManagerLeaderIP = restTemplate.getForObject(get_leader_uri, String.class);
//
//            if (!DBManagerLeaderIP.isEmpty()) {
//                break;
//            }
//        }
//        String set_primary_uri = NetworkConstants.getDBManagerSetPrimaryURI(DBManagerLeaderIP, String.valueOf(newIsFirstPrimaryCluster));
//        restTemplate.getForObject(set_primary_uri, String.class);
//    }
//
//    public static boolean getIsFirstClusterPrimary() {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        String DBManagerLeaderIP = "";
//
//        for (String DBManagerIP : DB_MANAGER_IP){
//            String get_leader_uri = NetworkConstants.getDBManagerLeaderURI(DBManagerIP);
//            DBManagerLeaderIP = restTemplate.getForObject(get_leader_uri, String.class);
//
//            if (!DBManagerLeaderIP.isEmpty()) {
//                break;
//            }
//        }
//
//        String uri = NetworkConstants.getDBManagerPrimaryURI(DBManagerLeaderIP);
//        ResponseEntity<Boolean> isFirstClusterPrimary = restTemplate.getForEntity(uri, Boolean.class);
//        return isFirstClusterPrimary.getBody();
//    }

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

    private static String sendDelete(JsonNode rq, String IP){
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerDeleteURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> deleteRq = new HttpEntity<String>(rq.toString(), headers);
        ResponseEntity<String> deleteList = rt.postForEntity(URI, deleteRq, String.class);
        return deleteList.getBody();
    }

    public static void sendShare(JsonNode rq){
        for(String IP : NetworkConstants.DB_MANAGER_IP){
            try{
                sendShare(rq,IP);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return;
    }

    private static void sendShare(JsonNode rq, String IP) {
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerShareURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> shareRq = new HttpEntity<String>(rq.toString(), headers);
        rt.postForEntity(URI, shareRq, String.class);
    }


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

    public static void sendWriteToLeader(String IP, JsonNode request){
        RestTemplate rt = new RestTemplate();
        String request_queue_uri = NetworkConstants.getRequestQueuePushURI(IP);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> rqUpdate = new HttpEntity<String>(request.toString(), headers);
        rt.postForEntity(request_queue_uri, rqUpdate, String.class);
    }

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

    public static int getRequestHead(String IP, String filename){
        RestTemplate restTemplate = new RestTemplate();
        String getHeadURI = NetworkConstants.getRequestQueueHeadURI(IP,filename);
        ResponseEntity<Integer> currOrder = restTemplate.getForEntity(getHeadURI,Integer.class);
        return currOrder.getBody();
    }

    public static void releaseLock(String IP, String filename){
        RestTemplate restTemplate = new RestTemplate();
        String removeHeadURI = NetworkConstants.getRequestQueueRemoveHeadURI(IP,filename);
        restTemplate.getForEntity(removeHeadURI,String.class);
    }




}
