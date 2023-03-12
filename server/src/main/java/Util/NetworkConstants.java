package Util;

import java.util.List;

public final class NetworkConstants {
    public static final String[] SERVER_IPS = new String[]{"172.31.0.104","172.31.14.187", "172.31.1.137"};
    public static int  REQUEST_QUEUE_SOCKET_PORT = 80;
    public static int  RESPONSE_QUEUE_SERVER_PORT = 9090;
    public static int MAIN_SERVER_PORT = 8080;
    public static int REQUEST_QUEUE_PORT = 8080;
    public static int RESPONSE_QUEUE_PORT = 8080;
    public static int DATABASE_MANAGER_PORT = 8080;
    public static String[] REQUEST_QUEUE_IPS = new String[] {"18.189.196.143","3.136.94.117"};
    public static String[] RESPONSE_QUEUE_IPS = new String[] {"18.224.111.143","18.216.20.109"};

    public static String getRequestQueueURI(String IP){
        return String.format("http://%s:%s/api/request/fetch",IP,REQUEST_QUEUE_PORT);
    }

    public static String getRequestQueueURIPing(String IP){
        return String.format("http://%s:%s/api/request/ping",IP,REQUEST_QUEUE_PORT);
    }

    public static String getRequestQueuePushURI(String IP){
        return String.format("http://%s:%s/api/request/push",IP,REQUEST_QUEUE_PORT);
    }

    public static String getProcessingServerURILeader(String IP){
        return String.format("http://%s:%s/leader/requestqueue",IP,MAIN_SERVER_PORT);
    }

    public static String getProcessingServerURILeaderServer(String IP){
        return String.format("http://%s:%s/leader/server",IP,MAIN_SERVER_PORT);
    }

    public static String getProcessingServerURIElection(String IP){
        return String.format("http://%s:%s/election",IP,MAIN_SERVER_PORT);
    }

    public static String getResponseQueueURI(String IP){
        return String.format("http://%s:%s/api/response/post",IP,RESPONSE_QUEUE_PORT);
    }

    public static String getResponseQueueLeaderURI(String IP){
        return String.format("http://%s:%s/api/request/leader",IP,RESPONSE_QUEUE_PORT);
    }

    public static String getDBManagerURI(){
        return String.format("http://172.31.14.172:%s/dbmanager/upload",DATABASE_MANAGER_PORT);
    }
}
