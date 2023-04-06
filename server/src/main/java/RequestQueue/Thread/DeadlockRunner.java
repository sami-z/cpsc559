package RequestQueue.Thread;

import RequestQueue.DataAccessObject.FileQueue;

public class DeadlockRunner implements Runnable{

    public static int  MAX_TIMEOUT = 20000;
    private int currOrder;
    private String key;
    private FileQueue fq;
    
    public DeadlockRunner(int currOrder, String key, FileQueue fq){
        this.currOrder = currOrder;
        this.key = key;
        this.fq = fq;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(MAX_TIMEOUT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(fq.getHead(key) == currOrder){
            fq.removeHead(key);
        }
    }
}
