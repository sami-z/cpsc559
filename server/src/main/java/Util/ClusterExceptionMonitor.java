package Util;

import com.mongodb.MongoException;

public class ClusterExceptionMonitor implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof MongoException) {
            NetworkUtil.DBManagerNotifyPrimaryChange(false, true);
            NetworkUtil.processingServerNotifyPrimaryChange(false);
        }
    }
}
