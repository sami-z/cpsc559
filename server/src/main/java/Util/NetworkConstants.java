package Util;

public final class NetworkConstants {
    public static final String[] SERVER_IPS = new String[]{"localhost"};
    public static int  REQUEST_QUEUE_SOCKET_PORT = 80;
    public static int  RESPONSE_QUEUE_SERVER_PORT = 9090;
    public static int MAIN_SERVER_PORT = 8080;

    public static int REQUEST_QUEUE_PORT = 8081;
    public static int RESPONSE_QUEUE_PORT = 8082;
    public static int DATABASE_MANAGER_PORT = 8083;
    public static String[] REQUEST_QUEUE_IPS = new String[] {"localhost"};
    public static String[] RESPONSE_QUEUE_IPS = new String[] {"localhost"};
    public static String DB_MANAGER_IP = "localhost";


    public static String getRequestQueueURI(String IP){
        return String.format("http://%s:%s/api/request/fetch",IP,REQUEST_QUEUE_PORT);
    }

    public static String getRequestQueueURIPing(String IP){
        return String.format("http://%s:%s/api/request/ping",IP,REQUEST_QUEUE_PORT);
    }

    public static String getRequestQueuePushURI(String IP) {
        return String.format("http://%s:%s/api/request/push", IP, REQUEST_QUEUE_PORT);
    }

    public static String getProcessingServerURIPing(String IP){
        return String.format("http://%s:%s/leader/ping",IP,MAIN_SERVER_PORT);

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

    public static String getRequestQueueLeaderURI(String IP){
        return String.format("http://%s:%s/api/request/leader",IP,RESPONSE_QUEUE_PORT);
    }

    public static String getDBManagerURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/upload",IP,DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerDeleteURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/delete",IP,DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerShareURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/share",IP,DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerGetHeadURI(String key) {
        return String.format("http://%s:%s/api/dbmanager/get-head/%s", DB_MANAGER_IP,DATABASE_MANAGER_PORT, key);
    }

    public static String getDBManagerRegisterURI(String IP) {
        return String.format("http://%s:%s/api/dbmanager/register",IP, DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerLeaderURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/get-leader",IP, DATABASE_MANAGER_PORT);
    }

    public static String setDBManagerLeaderURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/leader",IP, DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerPingURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/ping",IP, DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerPrimaryURI(String IP){
        return String.format("http://%s:%s/api/dbmanager/get-primary", IP, DATABASE_MANAGER_PORT);
    }

    public static String getDBManagerSetPrimaryURI(String IP, String newIsFirstPrimaryCluster){
        return String.format("http://%s:%s/api/dbmanager/set-primary/%s", IP, DATABASE_MANAGER_PORT, newIsFirstPrimaryCluster);
    }

    public static String getRequestQueueHeadURI(String IP, String fileName){
        return String.format("http://%s:%s/api/request/get-head/%s",IP,REQUEST_QUEUE_PORT,fileName);
    }

    public static String getRequestQueueRemoveHeadURI(String IP, String fileName){
        return String.format("http://%s:%s/api/request/remove-head/%s",IP,REQUEST_QUEUE_PORT,fileName);
    }
}
