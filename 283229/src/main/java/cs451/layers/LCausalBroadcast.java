package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LCausalBroadcast extends Layer{
    private final int [][] CAUSALITY;
    private int [] vc;
    private int lsb;
    private final int MY_ID;
    private final int NUMBER_OF_HOSTS;
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<BroadcastMessage>> pending;

    public LCausalBroadcast(Layer topLayer, Parser parser){
        NUMBER_OF_HOSTS = parser.NUMBER_OF_HOSTS;
        MY_ID = parser.MY_ID;
        CAUSALITY = parser.getCause();
        lsb = 0;
        vc = new int [NUMBER_OF_HOSTS];
        for(int i = 0 ; i < vc.length; i++){ vc[i] = 0; }
        pending = new ConcurrentHashMap<>();
        for(int i = 0 ; i < NUMBER_OF_HOSTS; i++){
            pending.put(i+1, new ConcurrentSkipListSet<>((BroadcastMessage bm1, BroadcastMessage bm2) -> {
                if(bm1.getSeqNumber()<bm2.getSeqNumber())
                    return -1;
                else if(bm1.getSeqNumber()==bm2.getSeqNumber())
                    return 0;
                else
                    return 1;
            }));
        }

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
        vc[bim- 1]++;
        topLayer.deliveredFromBottom(bm);
        pending.get(bim).pollFirst();
        return true;
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        BroadcastMessage bm = (BroadcastMessage)m;
        pending.get(bm.getBroadcasterID()).add(bm);
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        ((BroadcastMessage) m).setVC(causalVC());
        downLayer.sentFromTop(m);
    }

    private int [] causalVC() {
        int [] causalVC = new int[vc.length];
        for(int i =0 ; i<vc.length; i++){
            causalVC[i] = CAUSALITY[MY_ID-1][i] == 1 ? vc[i] : -1;
        }
        causalVC[MY_ID-1] = lsb; lsb++;
        return causalVC;
    }

    private class LCBDeliveringThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                for(int i = 0; i< NUMBER_OF_HOSTS; i++){
                    while(!pending.get(i+1).isEmpty() &&
                            deliverVCIfPossible(pending.get(i+1).first()));
                }
            }
        }
    }
}