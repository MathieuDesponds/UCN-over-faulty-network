package cs451.layers;

import cs451.Message;
import cs451.Parser;

import java.util.HashMap;
import java.util.HashSet;

public class UniformReliableBroadcast extends Layer {
    private final int MY_ID;
    //ToDo make it concurrent
    private HashSet<Message> mDelivered;
    private HashSet<Message> mPending;
    private HashMap<Message, Integer> mAcked;

    public UniformReliableBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        Layer downLayer = new BestEffortBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        mDelivered = new HashSet<>();
        mPending = new HashSet<>();
        mAcked = new HashMap<>();
    }

    @Override
    public void deliveredFromBottom(Message m) {

    }

    @Override
    public void sendFromTop(Message m) {
        downLayer.sendFromTop(m);
    }
}
