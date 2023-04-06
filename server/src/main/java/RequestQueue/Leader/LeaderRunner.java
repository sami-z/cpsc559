package RequestQueue.Leader;

import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClientException;
import sun.nio.ch.Net;

import java.util.HashMap;

public class LeaderRunner implements Runnable{

   private boolean requestSentToLeader = false;
   private JsonNode request;


   public static String getLeader(){
       HashMap<String, Integer> mapping = new HashMap<>();
       for(String IP : NetworkConstants.REQUEST_QUEUE_IPS){
           try{
               String leaderIP = NetworkUtil.getRequestQueueLeader(IP);
               mapping.put(leaderIP, mapping.getOrDefault(leaderIP,0)+1);
           }catch (RestClientException e){
               System.out.println("Could not get Leader for IP: " + IP);
           }
       }

       int currMax = -1;
       String ret = "";

       for(String leaderIP : mapping.keySet()){
           if(mapping.get(leaderIP)>currMax){
               currMax = mapping.get(leaderIP);
               ret = leaderIP;
           }
       }

       return ret;
   }


   public LeaderRunner(JsonNode req){
       this.request = req;
   }

    @Override
    public void run() {
       int trys = 0;
        while(!requestSentToLeader && trys< NetworkConstants.MAX_RETRY){
            try{
                NetworkUtil.sendWriteToLeader(LeaderState.leaderIP,request);
                this.requestSentToLeader = true;
            } catch (RestClientException e){
                System.out.println("Could not send to leader for " + request.asText());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                ServerState.leaderIP = getLeader();
                trys++;
            }
        }


    }
}
