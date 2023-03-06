package Util;

import java.util.List;

public final class NetworkConstants {
    public static int  REQUEST_QUEUE_SOCKET_PORT = 80;
    public static int  RESPONSE_QUEUE_SERVER_PORT = 9090;
    public static final List<String> SERVER_IPS = null;

    public static int MAIN_SERVER_PORT = 9091;
    public static int REQUEST_QUEUE_PORT = 9092;
    public static int RESPONSE_QUEUE_PORT = 9093;
    public static int DATABASE_MANAGER_PORT = 9094;


    public static String getRequestQueueURI(){
        return String.format("http://localhost:%s/api/request/fetch",REQUEST_QUEUE_PORT);
    }

    public static String getResponseQueueURI(){
        return String.format("http://localhost:%s/api/response",RESPONSE_QUEUE_PORT);
    }
}
