package Util;

import MainServer.ServerState;
import RequestQueue.Leader.LeaderState;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.time.Duration;

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

    public static boolean sendWrite(JsonNode rq){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getDBManagerURI();

        HttpEntity<String> request =
                new HttpEntity<String>(rq.toString(), headers);

        ResponseEntity<Boolean> wasReplaced = restTemplate.postForEntity(uri,request,Boolean.class);
        return wasReplaced.getBody();
    }

    public static String sendDelete(JsonNode rq){
        RestTemplate rt = new RestTemplate();
        String URI = NetworkConstants.getDBManagerDeleteURI();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> deleteRq = new HttpEntity<String>(rq.toString(), headers);
        ResponseEntity<String> deleteList = rt.postForEntity(URI, deleteRq, String.class);
        return deleteList.getBody();
    }

    public static boolean sendRegister(JsonNode rq){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = NetworkConstants.getDBManagerRegisterURI();

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
        String getHeadURI = NetworkConstants.getDBManagerGetHeadURI(fileName);
        ResponseEntity<Long> timestamp = restTemplate.getForEntity(getHeadURI, Long.class);
        return timestamp.getBody();
    }

    public static int obtainLock(String IP, String filename){
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
