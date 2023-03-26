package DatabaseManager.Service;

import DatabaseManager.DataAccessObject.DatabaseTimestamps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHandler {
    private final DatabaseTimestamps databaseTimestamps;

    @Autowired
    public DatabaseHandler(@Qualifier("databaseTimestamps") DatabaseTimestamps databaseTimestamps) {
        this.databaseTimestamps = databaseTimestamps;
    }

    public void updateTimestamp(String fileName, long timestamp) {
        databaseTimestamps.updateLatestTimestamp(fileName, timestamp);
    }

    public long getTimestamp(String fileName) {
        return databaseTimestamps.getLatestTimestamp(fileName);
    }
}
