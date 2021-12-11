package cs451.layers;

import cs451.Host;
import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Messages.Packet;
import cs451.Parsing.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageGrouper extends Layer {
    private int MY_ID;
    private final int MAX_M_BY_PKT = 2000;
    private final int NAGLE_TIMEOUT = 200; //ms
    List<Packet> pktByDst;
    ConcurrentLinkedDeque<BroadcastMessage> mToSend;

    Thread batchT;

    public MessageGrouper(Layer topLayer, Parser parser) {

        this.MY_ID = parser.myId();

        pktByDst = new ArrayList<>();
        mToSend = new ConcurrentLinkedDeque<>();
        for(int i = 0; i < parser.NUMBER_OF_HOSTS+1; i++)
            pktByDst.add(null);
        //Init the buffer to the other hosts
        for(Host h :parser.hosts())
            initPkt(h.getId());
        batchT = new Thread(new BatchingThread());
        batchT.setDaemon(true); batchT.start();
        setTopLayer(topLayer);
        setDownLayer(new PerfectLink(this, parser));
    }

    @Override
    public <PKT extends Message> void deliveredFromBottom(PKT m) {
        Packet pkt = (Packet) m;
        for(BroadcastMessage bm : pkt.getBrcMessages()){
            topLayer.deliveredFromBottom(bm);
        }
    }

    @Override
    public <BM extends Message> void sentFromTop(BM m) {
        BroadcastMessage bm = (BroadcastMessage) m;
        mToSend.addLast(bm);

    }

    private void initPkt(int index){
        pktByDst.set(index,new Packet(MY_ID, index, Packet.MessageType.MESSAGE, System.currentTimeMillis()));
    }

    private class BatchingThread implements Runnable {
        private long lastCheck;
        private final long TIME_BETWEEN_CHECK = 20;
        @Override
        public void run() {
            lastCheck = System.currentTimeMillis();
            while(true){
                while(!mToSend.isEmpty()){
                    BroadcastMessage bm = mToSend.pollFirst();
                    Packet pkt = pktByDst.get(bm.getDstId());
                    pkt.addBM(bm);
                    if(pkt.getSize() == MAX_M_BY_PKT){
                        initPkt(pkt.getDstID());
                        downLayer.sentFromTop(pkt);
                    }
                }
                long currentTime = System.currentTimeMillis();
                if(currentTime-lastCheck > TIME_BETWEEN_CHECK) {
                    lastCheck=currentTime;
                    for (Packet pkt : pktByDst) {
                        if (pkt != null && pkt.getSize() > 0 && currentTime - pkt.getTimeCreated() > NAGLE_TIMEOUT) {
                            downLayer.sentFromTop(pkt);
                            initPkt(pkt.getDstID());
                        }
                    }
                }
            }
        }
    }
}