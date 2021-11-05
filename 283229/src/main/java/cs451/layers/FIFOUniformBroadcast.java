package cs451.layers;

import cs451.BroadcastMessage;
import cs451.Message;
import cs451.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FIFOUniformBroadcast extends Layer {
    private int [] waitingFor;
    private List<TreeSet<BroadcastMessage>> pending;

    public FIFOUniformBroadcast(Layer topLayer, Parser parser){
        Layer downLayer = new UniformReliableBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);
        System.out.println("hosts : "+parser.NUMBER_OF_HOSTS);

        waitingFor = new int [parser.NUMBER_OF_HOSTS+1];
        for(int i = 0 ; i < waitingFor.length; i++){
            waitingFor[i] = 1;
        }
        pending = new ArrayList<TreeSet<BroadcastMessage>>();
        for(int i = 0 ; i < parser.NUMBER_OF_HOSTS+1; i++){
            pending.add(new TreeSet<BroadcastMessage>((BroadcastMessage bm1, BroadcastMessage bm2) -> {
                if(bm1.getSeqNumber()<bm2.getSeqNumber())
                    return -1;
                else if(bm1.getSeqNumber()==bm2.getSeqNumber())
                    return 0;
                else
                    return 1;
            }));
        }


    }
    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        int bid = m.getBroadcasterID();
        if(m.getSeqNumber() == waitingFor[bid]){
            topLayer.deliveredFromBottom(m);
            waitingFor[bid] ++;

            while(!pending.get(bid).isEmpty() &&
                    pending.get(bid).first().getSeqNumber() == waitingFor[bid]){
                topLayer.deliveredFromBottom(pending.get(bid).pollFirst());
                waitingFor[bid] ++;
            }
        }else{
            pending.get(m.getBroadcasterID()).add((cs451.BroadcastMessage) m);
        }
    }

    @Override
    public <BroadcastMessage extends Message> void sendFromTop(BroadcastMessage m) {
        downLayer.sendFromTop(m);
    }

    private class FIFOUBDeliveringThread implements Runnable {

        @Override
        public void run() {

        }
    }
}
