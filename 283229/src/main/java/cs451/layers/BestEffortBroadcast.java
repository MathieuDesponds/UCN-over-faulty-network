package cs451.layers;

import cs451.Host;
import cs451.Messages.BroadcastMessage;
import cs451.Messages.BroadcastMessageSent;
import cs451.Messages.Message;
import cs451.Parsing.Parser;

import java.util.List;

public class BestEffortBroadcast extends Layer {
    private final int MY_ID;
    private List<Host> hosts;

    public BestEffortBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        Host me = parser.getHostWithId(MY_ID);

        hosts = parser.hosts();
        hosts.remove(me);

        setTopLayer(topLayer);
        setDownLayer(new MessageGrouper(this, parser));
    }

    @Override
    public <BM extends Message> void deliveredFromBottom(BM m) {
        topLayer.deliveredFromBottom(m);
    }

    @Override
    public <BM extends Message> void  sentFromTop(BM m) {
        BroadcastMessage bm = (BroadcastMessage) m;
        for (Host h : hosts) {
            downLayer.sentFromTop(new BroadcastMessageSent(bm,h.getId()));
        }
        if(bm.getBroadcasterID() == MY_ID)
            topLayer.deliveredFromBottom(bm);
    }
}
