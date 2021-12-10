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

    private final int ESTIMATED_RTT_0 = 500;
    private final int DEVIATION_RTT_0 = 125;
    private final int TIMEOUT_INTERVAL_0 = 1000;
    private final int NEXT_TIMEOUT_0 = 0;
    private long [] nextTimeOut;
    private int [] estimatedRTT;
    private int [] deviationRTT;
    private int [] timeoutInterval;
    private final double alpha = 0.125; //Recommended 0.125
    private final double beta = 0.25;

    //Thread
    Thread plTOT;
    private ConcurrentHashMap<Packet,Boolean> mOnTheRoad;

    public PerfectLink(Layer topLayer,  Parser parser) {
        mReceived = new HashSet<>();

        this.NUMBER_OF_HOSTS = parser.hosts().size();
        waitingFor = new int[NUMBER_OF_HOSTS + 2];
        estimatedRTT = new int[NUMBER_OF_HOSTS + 2];
        deviationRTT = new int[NUMBER_OF_HOSTS + 2];
        timeoutInterval = new int[NUMBER_OF_HOSTS + 2];
        nextTimeOut = new long[NUMBER_OF_HOSTS + 2];
        for (int i = 0; i < waitingFor.length; i++) {
            waitingFor[i] = 1;
            estimatedRTT[i] = ESTIMATED_RTT_0;
            deviationRTT[i] = DEVIATION_RTT_0;
            timeoutInterval[i] = TIMEOUT_INTERVAL_0;
            nextTimeOut[i] = NEXT_TIMEOUT_0;
        }

        mOnTheRoad = new ConcurrentHashMap<>();
        System.out.println(NUMBER_OF_HOSTS);
        plTOT = new Thread(new PLTimeoutThread());
        plTOT.setDaemon(true);
        plTOT.start();
        super.setTopLayer(topLayer);
        super.setDownLayer(new FairLossLink(this, parser));
    }

    private void timeout(Packet m) {
        int dstID = m.getDstID();
        if(System.currentTimeMillis() > nextTimeOut[dstID]) {
            timeoutInterval[dstID] = (int) (timeoutInterval[dstID] * MULTIPLICATE_WHEN_TIMEOUT);
            nextTimeOut[dstID] = System.currentTimeMillis() + timeoutInterval[dstID];
        }
        mOnTheRoad.remove(m);
        sendToBottom(m);
    }

    private void updateTimeout(Long timeSent, int dstID) {
        long sampleRtt = System.currentTimeMillis()-timeSent;
        estimatedRTT[dstID] = (int) ((1-alpha) * estimatedRTT[dstID]  + alpha * sampleRtt);
        deviationRTT[dstID]  = (int) ((1-beta) * deviationRTT[dstID]  + beta * Math.abs(sampleRtt-estimatedRTT[dstID] ));
        timeoutInterval[dstID]  = estimatedRTT[dstID]  + 2*deviationRTT[dstID] ;
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
            updateTimeout(pkt.getTimeSent(), pkt.getSrcID());
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
                    Set<Packet> s = new HashSet<Packet>(mOnTheRoad.keySet());
                    for (Packet m : s) {
                        if (time - m.getTimeSent() > timeoutInterval[m.getDstID()]) {
                            timeout(m);
                        }
                    }
                }
            }
        }
    }
}
