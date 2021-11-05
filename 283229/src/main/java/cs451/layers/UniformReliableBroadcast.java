package cs451.layers;

import cs451.BroadcastMessage;
import cs451.Message;
import cs451.Parser;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast extends Layer {
    private final int MY_ID;
    private final int NUMBER_OF_HOSTS;
    private final int MIN_NUMBER_OF_HOSTS_TO_ACK;
    //ToDo make it concurrent
    private HashSet<BroadcastMessage> mDelivered;
    private ConcurrentHashMap<BroadcastMessage, Integer> mPendingToBeAcked;

    Thread URBAck;

    public UniformReliableBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        NUMBER_OF_HOSTS = parser.hosts().size();
        MIN_NUMBER_OF_HOSTS_TO_ACK = NUMBER_OF_HOSTS /2 +1;
        Layer downLayer = new BestEffortBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        mDelivered = new HashSet<>();
        mPendingToBeAcked = new ConcurrentHashMap<>();

        URBAck = new Thread(new URBDeliveringThread());
        URBAck.setDaemon(true);
        URBAck.start();
    }

    @Override
    public <BroadcastMessage extends Message> void  deliveredFromBottom(BroadcastMessage m) {
        if(!mDelivered.contains(m)) {
            int count = mPendingToBeAcked.getOrDefault(m, 0);
            if(count == 0 && m.getBroadcasterID() != MY_ID) {
                count++; //We know that already 2 (respectively me and the one that sent it) can deliver
                downLayer.sendFromTop(m);
            }
            mPendingToBeAcked.put((cs451.BroadcastMessage) m,count+1);
        }
    }

    @Override
    public void sendFromTop(Message m) {
        m.setBroadcasterID(MY_ID);
        downLayer.sendFromTop(m);
    }

    private class URBDeliveringThread implements Runnable{

        @Override
        public void run() {
            while(!closed){
                for (Map.Entry<BroadcastMessage, Integer> entry : mPendingToBeAcked.entrySet()) {
                    if(entry.getValue() >= MIN_NUMBER_OF_HOSTS_TO_ACK){
                        BroadcastMessage bm  = entry.getKey();
                        topLayer.deliveredFromBottom(bm);
                        mDelivered.add(bm);
                        mPendingToBeAcked.remove(bm);
                    }
                }
            }
        }
    }
}
