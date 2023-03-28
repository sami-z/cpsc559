package DatabaseManager.DataAccessObject;

import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository("databaseTimestamps")
public class DatabaseTimestampsImpl implements DatabaseTimestamps{

    private static final HashMap<String, Long> timestampMap = new HashMap<>();

    @Override
    public void updateLatestTimestamp(String userName, String fileName, long timestamp) {
        String key = String.join(",", userName, fileName);
        timestampMap.put(key, timestamp);
    }

    @Override
    public long getLatestTimestamp(String key) {
        if (!timestampMap.containsKey(key)) return -1;

        return timestampMap.get(key);
    }
}
