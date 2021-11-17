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

        this.MY_ID = parser.myId();

        pktByDst = new ArrayList<>();
        for(int i = 0; i < parser.NUMBER_OF_HOSTS+1; i++)
            pktByDst.add(null);
        //Init the buffer to the other hosts
        for(Host h :parser.hosts())
            initPkt(h.getId());

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
        Packet pkt = pktByDst.get(bm.getDstId());
        pkt.addBM(bm);
        if(pkt.getSize() == MAX_M_BY_PKT){
            downLayer.sentFromTop(pkt);
            initPkt(pkt.getDstID());
        }
    }

    private void initPkt(int index){
        pktByDst.set(index,new Packet(MY_ID, index, Packet.MessageType.MESSAGE));
    }
}
