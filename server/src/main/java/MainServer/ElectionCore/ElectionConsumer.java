package MainServer.ElectionCore;

import MainServer.ElectionCore.State.ElectionState;
import MainServer.Monitor.RequestQueueMonitor;
import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ElectionConsumer {

    private static boolean response = false;

    public static void sendLeader(String IP){

    }

    public static void sendElection(String IP){

    }

    public static void initiatieElection() throws UnknownHostException, InterruptedException {
        ServerState.isElectionRunning = true;
        List<InetAddress> higher = new ArrayList<>();

        for(String ip : NetworkConstants.SERVER_IPS){
            InetAddress currIP = InetAddress.getByName(ip);
            if(NetworkUtil.isGreater(currIP,es.serverIP)){
                higher.add(currIP);
            }
        }

        if(higher.size() == 0){

            return;
        }

        for(String IP : higher){
            sendElection(IP);
        }

        ElectionConsumer.response = false;
        Thread.sleep(1000);
        if(ElectionConsumer.response){
        }else{

        }

    }

    public void setLeader(){
        for (String IP : NetworkConstants.SERVER_IPS){
            sendLeader(IP);
        }
        new Thread(new RequestQueueMonitor()).start();
    }

    public ElectionConsumer(ElectionState initialSate){
        this.es = initialSate;
    }

}
