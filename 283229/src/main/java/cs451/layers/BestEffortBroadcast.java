package cs451.layers;

import cs451.Host;
import cs451.Message;
import cs451.Parser;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BestEffortBroadcast extends Layer {
    private List<Host> hosts;
    private String ip;
    private int port;
    private int id;

    Thread bebST;
    private ConcurrentLinkedDeque<Message> mToSend;

    public BestEffortBroadcast(Layer topLayer, Parser parser){
        Host me = parser.getMe();
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
        topLayer.deliveredFromBottom(m);
    }

    @Override
    public void sendFromTop(Message m) {
        mToSend.addLast(m);
    }

    private class BEBSendingThread implements Runnable {
        @Override
        public void run() {
            while(!closed){
                if(!mToSend.isEmpty()) {
                    Message m = mToSend.pollFirst();
                    for (Host h : hosts) {
                        m.setClientServer(id, h.getId());
                        downLayer.sendFromTop(new Message(m));
                    }
                    deliveredFromBottom(m);
                }
            }
        }
    }
}
