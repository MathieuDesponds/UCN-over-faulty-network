package cs451.Messages;

public class BroadcastMessageReceived extends BroadcastMessage{
    private long sumVC; // Only when received
    private int byteSize; //Only when sent

    public BroadcastMessageReceived(int seqNumber, int broadcasterID, String payload, int [] vc, int byteSize) {
        super(seqNumber, broadcasterID, payload,vc);
        for(int i= 0; i<vc.length; i++){
            sumVC += vc[i];
        }
        this.byteSize = byteSize;
    }

    public long getSumVC() {
        return sumVC;
    }

    public int getByteSize() {
        return byteSize;
    }
}
