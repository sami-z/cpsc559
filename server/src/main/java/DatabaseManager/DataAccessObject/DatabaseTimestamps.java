package DatabaseManager.DataAccessObject;

public interface DatabaseTimestamps {
    void updateLatestTimestamp(String fileName, long timestamp);

    long getLatestTimestamp(String fileName);
}
