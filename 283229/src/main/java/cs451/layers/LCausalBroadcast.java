package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.concurrent.PriorityBlockingQueue;

public class LCausalBroadcast extends Layer{
    private int [] vc;
    private PriorityBlockingQueue<BroadcastMessage> pending;

    public LCausalBroadcast(Layer topLayer, Parser parser){
        vc = new int [parser.NUMBER_OF_HOSTS];
        for(int i = 0 ; i < vc.length; i++){ vc[i] = 0; }
        pending = new PriorityBlockingQueue<>(0,(BroadcastMessage bm1, BroadcastMessage bm2) -> {
            long s1 = bm1.getSumVC(), s2 = bm2.getSumVC();
            if(s1<s2)
                return -1;
            else if(s1 == s2)
                return 0;
            else
                return 1;
        });
        addThread(new Thread(new LCBDeliveringThread()));

        Layer downLayer = new UniformReliableBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);
    }
    private boolean deliverVCIfPossible(BroadcastMessage bm){
        int [] bmVC = bm.getVC();
        for(int i = 0; i<bmVC.length; i++){
            if(bmVC[i] > vc[i]){
                return false;
            }
        }
        topLayer.deliveredFromBottom(bm);
        vc[bm.getBroadcasterID() - 1]++;
        return true;
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        BroadcastMessage bm = (BroadcastMessage)m;
        if(!deliverVCIfPossible(bm)) {
            pending.put(bm);
        }
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        ((BroadcastMessage) m).addVC(vc);
        downLayer.sentFromTop(m);
    }

    private class LCBDeliveringThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                while(!pending.isEmpty()) {
                    for(BroadcastMessage bm : pending) {
                        deliverVCIfPossible(bm);
                    }
                }
            }
        }

    }
}