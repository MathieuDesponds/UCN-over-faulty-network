package cs451.layers;

import cs451.Messages.Message;
import cs451.Messages.Packet;
import cs451.Messages.Packet.MessageType;
import cs451.Parsing.Parser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerfectLink extends Layer{
    private final int NUMBER_OF_HOSTS;
    private int [] waitingFor; //for receiver

    //Psendo SR
    private HashSet<Packet> mReceived;

    //TIMEOUT
    private final double MULTIPLICATE_WHEN_TIMEOUT = 2;
    private long nextTimeOut = 0;
    private int estimatedRTT = 500;
    private int deviationRTT = 125;
    private int timeoutInterval = 1000;
    private final double alpha = 0.25; //Recommended 0.125
    private final double beta = 0.25;

    //Thread
    Thread plTOT;
    private ConcurrentHashMap<Packet,Boolean> mOnTheRoad;

    public PerfectLink(Layer topLayer,  Parser parser) {
        super.setTopLayer(topLayer);
        super.setDownLayer(new FairLossLink(this, parser));


        mReceived = new HashSet<>();

        this.NUMBER_OF_HOSTS = parser.hosts().size();
        waitingFor = new int[NUMBER_OF_HOSTS + 1];
        for (int i = 0; i < waitingFor.length; i++)
            waitingFor[i] = 1;

        mOnTheRoad = new ConcurrentHashMap<>();

        plTOT = new Thread(new PLTimeoutThread());
        plTOT.setDaemon(true);
        plTOT.start();
    }

    private void timeout(Packet m) {
        if(System.currentTimeMillis() > nextTimeOut) {
            timeoutInterval = (int) (timeoutInterval * MULTIPLICATE_WHEN_TIMEOUT);
            nextTimeOut = System.currentTimeMillis() + timeoutInterval;
        }
        mOnTheRoad.remove(m);
        sendToBottom(m);
    }

    private void updateTimeout(Long timeSent) {
        long sampleRtt = System.currentTimeMillis()-timeSent;
        estimatedRTT = (int) ((1-alpha) * estimatedRTT + alpha * sampleRtt);
        deviationRTT = (int) ((1-beta) * deviationRTT + beta * Math.abs(sampleRtt-estimatedRTT));
        timeoutInterval = estimatedRTT + 2*deviationRTT;
    }


    @Override
    public <PKT extends Message> void deliveredFromBottom(PKT m) {
        Packet pkt = (Packet) m;
        if(pkt.getMessageType() == MessageType.MESSAGE){
            if(!mReceived.contains(pkt)) {
                mReceived.add(pkt);
                topLayer.deliveredFromBottom(pkt);
            }
            ackPacket(pkt);
        }else if(pkt.getMessageType() == MessageType.ACK){
            mOnTheRoad.remove(pkt.getAckedPacketToHash());
            updateTimeout(pkt.getTimeSent());
        }
    }
    private void ackPacket(Packet m){
        sendToBottom(m.getAckingPacket());
    }

    @Override
    public <PKT extends Message> void sentFromTop(PKT m) {
        sendToBottom((Packet) m);
    }

    private void sendToBottom(Packet pkt){
        if(pkt.getMessageType() == MessageType.MESSAGE){
            pkt.setTimeSent(System.currentTimeMillis());
            mOnTheRoad.put(pkt,true);
        }
        downLayer.sentFromTop(pkt);
    }

    private class PLTimeoutThread implements Runnable{

        @Override
        public void run() {
            long lastCheck = 0;
            long timeBetweenCheck = 20;
            while(!closed){
                long time = System.currentTimeMillis();
                if(time-lastCheck > timeBetweenCheck) {
                    lastCheck = time;
                    int to = timeoutInterval;
                    Set<Packet> s = new HashSet<>(mOnTheRoad.keySet());
                    for (Packet m : s) {
                        if (time - m.getTimeSent() > to) {
                            timeout(m);
                        }
                    }
                }
            }
        }
    }
}
