package MainServer.ElectionCore;

import MainServer.Monitor.RequestQueueMonitor;
import MainServer.ServerState;
import Util.NetworkConstants;
import Util.NetworkUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ElectionConsumer {

    public static boolean response = false;
    public static boolean isBullied = false;

    public static void sendLeader(String IP){

    }

    public static void sendElection(String IP){

    }

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

    public static void setLeader(){
        for (String IP : NetworkConstants.SERVER_IPS){
            sendLeader(IP);
        }
        new Thread(new RequestQueueMonitor()).start();
    }

}
