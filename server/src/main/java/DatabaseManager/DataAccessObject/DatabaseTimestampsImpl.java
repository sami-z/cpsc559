package DatabaseManager.DataAccessObject;

import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository("databaseTimestamps")
public class DatabaseTimestampsImpl implements DatabaseTimestamps{

    private static final HashMap<String, Long> timestampMap = new HashMap<>();

    /**

     Updates the latest timestamp for a given user and file.
     @param userName the name of the user
     @param fileName the name of the file
     @param timestamp the latest timestamp to be updated
     */
    @Override
    public void updateLatestTimestamp(String userName, String fileName, long timestamp) {
        String key = String.join(",", userName, fileName);
        timestampMap.put(key, timestamp);
    }

    /**

     Returns the latest timestamp for the specified key.
     @param key the key for which to retrieve the latest timestamp
     @return the latest timestamp for the specified key, or -1 if the key is not found in the timestamp map
     */
    @Override
    public long getLatestTimestamp(String key) {
        if (!timestampMap.containsKey(key)) return -1;

        return timestampMap.get(key);
    }
}
