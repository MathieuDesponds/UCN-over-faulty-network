package cs451.layers;

import cs451.Messages.BroadcastMessage;
import cs451.Host;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BestEffortBroadcast extends Layer {
    private final int MY_ID;
    private List<Host> hosts;

    Thread bebST;
    private ConcurrentLinkedDeque<BroadcastMessage> mToSend;

    public BestEffortBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        Host me = parser.getHostWithId(MY_ID);

        hosts = parser.hosts();
        hosts.remove(me);

        setTopLayer(topLayer);
        setDownLayer(new MessageGrouper(this, parser));

        mToSend = new ConcurrentLinkedDeque<>();
        bebST = new Thread(new BEBSendingThread());
        bebST.setDaemon(true);
        bebST.start();
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        //System.out.println("receive"+m);
        topLayer.deliveredFromBottom(m);
    }

    @Override
    public <BM extends Message> void  sentFromTop(BM m) {
        mToSend.addLast((BroadcastMessage) m);
    }

    private class BEBSendingThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                if(!mToSend.isEmpty()) {
                    BroadcastMessage bm = mToSend.pollFirst();
                    for (Host h : hosts) {
                        //System.out.println("send "+ new BroadcastMessage(bm,h.getId()));
                        downLayer.sentFromTop(new BroadcastMessage(bm,h.getId()));
                    }
                    if(bm.getBroadcasterID() == MY_ID)
                        topLayer.deliveredFromBottom(bm);
                }
            }
        }
    }
}
