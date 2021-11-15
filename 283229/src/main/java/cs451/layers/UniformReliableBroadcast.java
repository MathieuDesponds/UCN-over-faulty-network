package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast extends Layer {
    private final int MY_ID;
    private final int NUMBER_OF_HOSTS;
    private final int MIN_NUMBER_OF_HOSTS_TO_ACK;
    private ConcurrentHashMap<BroadcastMessage,Boolean> mDelivered;
    private ConcurrentHashMap<BroadcastMessage, Integer> mPendingToBeAcked;

    Thread URBAck;

    public UniformReliableBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        NUMBER_OF_HOSTS = parser.hosts().size();
        MIN_NUMBER_OF_HOSTS_TO_ACK = NUMBER_OF_HOSTS /2 +1;
        Layer downLayer = new BestEffortBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        mDelivered = new ConcurrentHashMap<>();
        mPendingToBeAcked = new ConcurrentHashMap<>();

        URBAck = new Thread(new URBDeliveringThread());
        URBAck.setDaemon(true);
        URBAck.start();
    }

    @Override
    public <BM extends Message> void  deliveredFromBottom(BM m) {
        if(!mDelivered.containsKey(m)) {
            //We checked how many times we received this BroadcastMessage
            int count = mPendingToBeAcked.getOrDefault(m, 0);
            if(count == 0 && ((BroadcastMessage) m).getBroadcasterID() != MY_ID) {
                count++; //We know that already 2 (respectively me and the one that sent it) can deliver
                downLayer.sentFromTop(m);
            }
            mPendingToBeAcked.put((BroadcastMessage) m,count+1);
        }
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        ((BroadcastMessage) m).setBroadcasterID(MY_ID);
        downLayer.sentFromTop(m);
    }

    private class URBDeliveringThread implements Runnable{
        @Override
        public void run() {
            while(!closed){
                for (Map.Entry<BroadcastMessage, Integer> entry : mPendingToBeAcked.entrySet()) {
                    if(entry.getValue() >= MIN_NUMBER_OF_HOSTS_TO_ACK){
                        BroadcastMessage bm  = entry.getKey();
                        topLayer.deliveredFromBottom(bm);
                        mDelivered.put(bm,true);
                        mPendingToBeAcked.remove(bm);
                    }
                }
            }
        }
    }
}
