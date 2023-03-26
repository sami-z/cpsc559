package DatabaseManager.DataAccessObject;

import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository("databaseTimestamps")
public class DatabaseTimestampsImpl implements DatabaseTimestamps{

    private static final HashMap<String, Long> timestampMap = new HashMap<>();

    @Override
    public void updateLatestTimestamp(String fileName, long timestamp) {
        timestampMap.put(fileName, timestamp);
    }

    @Override
    public long getLatestTimestamp(String fileName) {
        if (!timestampMap.containsKey(fileName)) return -1;

        return timestampMap.get(fileName);
    }
}
