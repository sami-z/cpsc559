package DatabaseManager.DataAccessObject;

public interface DatabaseTimestamps {
    void updateLatestTimestamp(String userName, String fileName, long timestamp);

    long getLatestTimestamp(String key);
}
