package cs451.layers;

import cs451.BroadcastMessage;
import cs451.Host;
import cs451.Message;
import cs451.Parser;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BestEffortBroadcast extends Layer {
    private final int MY_ID;
    private List<Host> hosts;
    private String ip;
    private int port;
    private int id;

    Thread bebST;
    private ConcurrentLinkedDeque<BroadcastMessage> mToSend;

    public BestEffortBroadcast(Layer topLayer, Parser parser){
        MY_ID = parser.myId();
        Host me = parser.getHostWithId(MY_ID);
        this.ip = me.getIp(); this.port = me.getPort(); this.id = me.getId();

        hosts = parser.hosts();
        hosts.remove(me);

        setTopLayer(topLayer);
        setDownLayer(new PerfectLink(this, ip, port, parser));

        mToSend = new ConcurrentLinkedDeque<>();
        bebST = new Thread(new BEBSendingThread());
        bebST.setDaemon(true);
        bebST.start();
    }

    @Override
    public void deliveredFromBottom(Message m) {
        System.out.println("receive"+m);
        topLayer.deliveredFromBottom(new BroadcastMessage(m));
    }

    @Override
    public <BroadcastMessage extends Message> void  sendFromTop(BroadcastMessage m) {
        mToSend.addLast((cs451.BroadcastMessage) m);
    }

    private class BEBSendingThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                if(!mToSend.isEmpty()) {
                    BroadcastMessage m = mToSend.pollFirst();
                    for (Host h : hosts) {
                        m.setClientServer(id, h.getId());
                        System.out.println("send"+m);
                        downLayer.sendFromTop(new Message(m));
                    }
                    if(m.getBroadcasterID() == MY_ID)
                        deliveredFromBottom(m);
                }
            }
        }
    }
}
