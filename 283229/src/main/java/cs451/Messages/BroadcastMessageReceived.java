package cs451.Messages;

public class BroadcastMessageReceived extends BroadcastMessage{
    private long sumVC; // Only when received
    public BroadcastMessageReceived(int seqNumber, int broadcasterID, String payload, int [] vc) {
        super(seqNumber, broadcasterID, payload,vc);
        for(int i= 0; i<vc.length; i++){
            sumVC += vc[i];
        }
    }

    public long getSumVC() {
        return sumVC;
    }
}
