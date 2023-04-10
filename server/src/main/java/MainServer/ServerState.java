package MainServer;


/**
 * Class used to store the global state of the current processing server
 * */

public class ServerState {
    public static String requestQueueIP = "";       // IP of leader request queue

    public static boolean requestQueueDown = false;

    public static String responseQueueIP = "";
    public static String DBManagerIP = "";      // IP of leadeer DBmanager
    public static String leaderIP = "";         // IP of leader server
    public static String serverIP = "";         // ip of current server
    public static boolean isElectionRunning;

}
