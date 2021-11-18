package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.concurrent.ConcurrentHashMap;

public class UniformReliableBroadcast extends Layer {
    private final int MY_ID;
    private final int NUMBER_OF_HOSTS;
    private final int MIN_NUMBER_OF_HOSTS_TO_ACK;
    private ConcurrentHashMap<BroadcastMessage,Boolean> mDelivered;
    private ConcurrentHashMap<BroadcastMessage, Integer> mPendingToBeAcked;


    public UniformReliableBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        NUMBER_OF_HOSTS = parser.hosts().size();
        MIN_NUMBER_OF_HOSTS_TO_ACK = NUMBER_OF_HOSTS /2 +1;
        Layer downLayer = new BestEffortBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        mDelivered = new ConcurrentHashMap<>();
        mPendingToBeAcked = new ConcurrentHashMap<>();
    }

    @Override
    public <BM extends Message> void  deliveredFromBottom(BM m) {
        BroadcastMessage bm = (BroadcastMessage) m;
        if(!mDelivered.containsKey(bm)) {
            //We checked how many times we received this BroadcastMessage
            int count = mPendingToBeAcked.getOrDefault(bm, 0);
            if(count == 0 && bm.getBroadcasterID() != MY_ID) {
                count++; //We know that already 2 (respectively me and the one that sent it) can deliver
                downLayer.sentFromTop(bm);
            }
            if(count+1 >= MIN_NUMBER_OF_HOSTS_TO_ACK){
                topLayer.deliveredFromBottom(bm);
                mDelivered.put(bm,true);
                mPendingToBeAcked.remove(bm);
            }else {
                mPendingToBeAcked.put((BroadcastMessage) m, count + 1);
            }
        }
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        ((BroadcastMessage) m).setBroadcasterID(MY_ID);
        downLayer.sentFromTop(m);
    }
}
