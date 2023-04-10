package RequestQueue.Thread;

import RequestQueue.DataAccessObject.FileQueue;

/**

 The DeadlockRunner class implements the Runnable interface to create a thread that is responsible for removing the head of the file queue for a specific key
 in case of a deadlock. If a request with a higher order value to the current order value is not processed within a certain timeout time, the DeadlockRunner
 removes the head from the queue so that other requests can proceed.
 */
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
