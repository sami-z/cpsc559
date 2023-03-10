package MainServer.ElectionCore;

import MainServer.ElectionCore.State.ElectionState;
import MainServer.Monitor.RequestQueueMonitor;
import Util.NetworkConstants;
import Util.NetworkUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ElectionConsumer {

    private static ElectionState es;

    public static void initiatieElection() throws UnknownHostException, InterruptedException {
        es.isRunning = true;
        List<InetAddress> higher = new ArrayList<>();

        for(String ip : NetworkConstants.SERVER_IPS){
            InetAddress currIP = InetAddress.getByName(ip);
            if(NetworkUtil.isGreater(currIP,es.serverIP)){
                higher.add(currIP);
            }
        }

        if(higher.size() == 0){

        }else{

        }

        Thread.sleep(1000);

    }

    public void setLeader(){
        new Thread(new RequestQueueMonitor()).start();

    }

    public ElectionConsumer(ElectionState initialSate){
        this.es = initialSate;
    }

}
