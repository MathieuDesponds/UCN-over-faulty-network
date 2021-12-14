package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.BroadcastMessageReceived;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class LCausalBroadcast extends Layer{
    private final int [][] CAUSALITY;
    private int [] vc;
    private int lsb;
    private final int MY_ID;
    private PriorityBlockingQueue<BroadcastMessageReceived> pending;

    public LCausalBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.MY_ID;
        CAUSALITY = parser.getCause();
        lsb = 0;
        vc = new int [parser.NUMBER_OF_HOSTS];
        for(int i = 0 ; i < vc.length; i++){ vc[i] = 0; }
        pending = new PriorityBlockingQueue<>(1,(BroadcastMessageReceived bm1, BroadcastMessageReceived bm2) ->{
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
        int bim = bm.getBroadcasterID();
        for (int i = 0; i < bmVC.length; i++) {
            if (bmVC[i] > vc[i]) {
                return false;
            }
        }
        vc[bm.getBroadcasterID() - 1]++;
        topLayer.deliveredFromBottom(bm);
        pending.remove(bm);
        return true;
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        BroadcastMessageReceived bm = (BroadcastMessageReceived)m;
        if(!deliverVCIfPossible(bm)) {
            pending.put(bm);
        }
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        ((BroadcastMessage) m).setVC(causalVC());
        downLayer.sentFromTop(m);
    }

    private int [] causalVC() {
        int [] causalVC = new int[vc.length];
        for(int i =0 ; i<vc.length; i++){
            causalVC[i] = CAUSALITY[MY_ID-1][i] == 1 ? vc[i] : 0;
        }
        causalVC[MY_ID-1] = lsb; lsb++;
        return causalVC;
    }

    private class LCBDeliveringThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                while(!pending.isEmpty()) {
//                    for(BroadcastMessage bm : pending) {
//                        deliverVCIfPossible(bm);
//                    }
                    Iterator<BroadcastMessageReceived> iter = pending.iterator();
                    while(iter.hasNext()) {
                        if(deliverVCIfPossible(iter.next()))
                            iter.remove();
                    }
                }
            }
        }
    }
}