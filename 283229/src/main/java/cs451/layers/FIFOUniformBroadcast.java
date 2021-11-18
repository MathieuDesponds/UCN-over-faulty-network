package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FIFOUniformBroadcast extends Layer {
    private int [] waitingFor;
    private ConcurrentHashMap<Integer,TreeSet<BroadcastMessage>> pending;
    private ConcurrentLinkedDeque<BroadcastMessage> mToDeliver;

    Thread fifoDT;

    public FIFOUniformBroadcast(Layer topLayer, Parser parser){
        Layer downLayer = new UniformReliableBroadcast(this, parser);
        super.setDownLayer(downLayer);
        super.setTopLayer(topLayer);

        waitingFor = new int [parser.NUMBER_OF_HOSTS+1];
        for(int i = 0 ; i < waitingFor.length; i++){
            waitingFor[i] = 1;
        }
        pending = new ConcurrentHashMap<>();
        for(int i = 0 ; i < parser.NUMBER_OF_HOSTS; i++){
            pending.put(i+1, new TreeSet<BroadcastMessage>((BroadcastMessage bm1, BroadcastMessage bm2) -> {
                if(bm1.getSeqNumber()<bm2.getSeqNumber())
                    return -1;
                else if(bm1.getSeqNumber()==bm2.getSeqNumber())
                    return 0;
                else
                    return 1;
            }));
        }
        mToDeliver = new ConcurrentLinkedDeque<>();
        fifoDT = new Thread(new FIFOUBDeliveringThread());
        fifoDT.setDaemon(true);
        fifoDT.start();

    }
    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        mToDeliver.addLast((BroadcastMessage) m);
    }

    @Override
    public <BroadcastMessage extends Message> void sentFromTop(BroadcastMessage m) {
        downLayer.sentFromTop(m);
    }

    private class FIFOUBDeliveringThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                if(!mToDeliver.isEmpty()){
                    BroadcastMessage m = mToDeliver.pollFirst();
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
                        pending.get(m.getBroadcasterID()).add(m);
                    }
                }
            }
        }
    }
}
