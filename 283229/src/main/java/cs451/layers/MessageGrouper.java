package cs451.layers;

import cs451.Host;
import cs451.Messages.BroadcastMessage;
import cs451.Messages.Message;
import cs451.Messages.Packet;
import cs451.Parsing.Parser;

import java.util.ArrayList;
import java.util.List;

public class MessageGrouper extends Layer {
    private int MY_ID;
    private final int MAX_M_BY_PKT = 100;
    List<Packet> pktByDst;

    public MessageGrouper(Layer topLayer, Parser parser) {
        setTopLayer(topLayer);
        setDownLayer(new PerfectLink(this, parser));

        Host me = parser.getME();
        this.MY_ID = parser.myId();

        pktByDst = new ArrayList<>();
        for(Host h :parser.hosts()){
            initPkt(h.getId());
        }
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
        Packet pkt = pktByDst.get(m.getBroadcasterID());
        pkt.addBM((BroadcastMessage)m);
        if(pkt.getSize() == MAX_M_BY_PKT){
            downLayer.sentFromTop(pkt);
            initPkt(m.getBroadcasterID());
        }
    }

    private void initPkt(int index){
        pktByDst.add(index,new Packet(MY_ID, MY_ID, index, Packet.MessageType.MESSAGE));
    }
}
