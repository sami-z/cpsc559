package RequestQueue.Leader;

import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;

public class LeaderRunner implements Runnable{

   private boolean requestSentToLeader = false;
   private JsonNode request;


   public static String getLeader(){
       HashMap<String, Integer> mapping = new HashMap<>();
       for(String IP : NetworkConstants.REQUEST_QUEUE_IPS){
           try{
               String leaderIP = NetworkUtil.getRequestQueueLeader(IP);
               if(leaderIP != null && !leaderIP.isEmpty())
                   mapping.put(leaderIP, mapping.getOrDefault(leaderIP,0)+1);
           }catch (RestClientException e){
               System.out.println("Could not get Leader for IP: " + IP);
           }
       }

       int currMax = -1;
       String ret = "";

       for(String leaderIP : mapping.keySet()){
           System.out.println("printing out leaderIP" + leaderIP);
           if(mapping.get(leaderIP)>currMax){
               System.out.println("Setting ret");
               currMax = mapping.get(leaderIP);
               ret = leaderIP;
           }
       }

       System.out.println("RET IS THIS: " + ret);

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
            } catch (Exception e){
                System.out.println("Could not send to leader for " + request.toPrettyString());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                LeaderState.leaderIP = getLeader();
                trys++;
            }
        }


    }
}
