package Util;

import java.net.InetAddress;

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

}
