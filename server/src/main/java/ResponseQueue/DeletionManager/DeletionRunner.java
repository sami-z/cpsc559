package ResponseQueue.DeletionManager;

import ResponseQueue.Service.ResponseQueueHandler;

public class DeletionRunner implements Runnable{

    private int timeout = 10000;

    private String currUser;
    private ResponseQueueHandler rh;

    public DeletionRunner(String currUser, ResponseQueueHandler rh){
        this.currUser = currUser;
        this.rh = rh;
    }

    @Override
    public void run() {
        while(true){

            long curr = rh.getHeadTime(currUser);
            long currTime = System.currentTimeMillis();

            if(currTime-curr > timeout) {
                System.out.println("POPPING FROM HEAD");
                rh.pop(currUser);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
