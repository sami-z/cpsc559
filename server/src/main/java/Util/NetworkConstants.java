package Util;

import java.util.List;

public final class NetworkConstants {
    public static final String[] SERVER_IPS = null;
    public static int  REQUEST_QUEUE_SOCKET_PORT = 80;
    public static int  RESPONSE_QUEUE_SERVER_PORT = 9090;
    public static int MAIN_SERVER_PORT = 8080;
    public static int REQUEST_QUEUE_PORT = 8080;
    public static int RESPONSE_QUEUE_PORT = 8080;
    public static int DATABASE_MANAGER_PORT = 8080;
    public static String[] REQUEST_QUEUE_IPS = new String[] {"18.189.196.143"};
    public static String[] RESPONSE_QUEUE_IPS = new String[] {"3.12.236.117"};



    public static String getRequestQueueURI(String IP){
        return String.format("http://%s:%s/api/request/fetch",IP,REQUEST_QUEUE_PORT);
    }

    public static String getRequestQueueURIPing(String IP){
        return String.format("http://%s:%s/api/request/ping",IP,REQUEST_QUEUE_PORT);
    }

    public static String getProcessingServerURILeader(String IP){
        return String.format("http://%s:%s/leader/requestqueue",IP,MAIN_SERVER_PORT);
    }

    public static String getResponseQueueURI(String IP){
        return String.format("http://%s:%s/api/response/post",IP,RESPONSE_QUEUE_PORT);
    }

    public static String getDBManagerURI(){
        return String.format("http://3.21.101.62:%s/dbmanager/upload",DATABASE_MANAGER_PORT);
    }
}
