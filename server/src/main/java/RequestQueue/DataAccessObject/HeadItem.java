package RequestQueue.DataAccessObject;

public class HeadItem {
    public int orderValue;
    public long currTime;
    public HeadItem(int orderValue, long currTime){
        this.currTime = currTime;
        this.orderValue = orderValue;
    }
}
