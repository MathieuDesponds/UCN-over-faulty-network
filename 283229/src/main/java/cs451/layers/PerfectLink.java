package cs451.layers;

import cs451.Message;
import cs451.Parser;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink extends Layer{
    private final int MY_ID;
    //Go-Back-N
    private final int WINDOW;
    private final int NUMBER_OF_HOSTS;
    private int base = 1; //Seq number of the base --> seq n is at list(n-1)
    private AtomicInteger mSentInWindow;
    private int [] waitingFor; //for receiver

    //Psendo SR
    private HashSet<Message> mReceived;

    //TIMEOUT
    private final double MULTIPLICATE_WHEN_TIMEOUT = 2;
    private long nextTimeOut = 0;
    private int estimatedRTT = 500;
    private int deviationRTT = 125;
    private int estimatedTimeout = 1000;
    private int timeoutInterval = 1000;
    private final double alpha = 0.25; //Recommended 0.125
    private final double beta = 0.25;
    //private ConcurrentLinkedDeque<Long> timeouts;

    //Thread
    Thread plRT,plST,plTOT;
    private ConcurrentLinkedDeque<Message> mToSend;
    private ConcurrentLinkedDeque<Message> waitingToBeSent;
    private ConcurrentLinkedDeque<Message> mToDeliver;
    private ConcurrentHashMap<Message,Boolean> mOnTheRoad;

    public PerfectLink(Layer topLayer, String ip, int port, Parser parser) {
        super.setTopLayer(topLayer);
        super.setDownLayer(new FairLossLink(this, ip, port));

        //Set no Window for the moment
        WINDOW = parser.configNbMessage();

        mReceived = new HashSet<>();

        this.MY_ID = parser.myId();
        this.NUMBER_OF_HOSTS = parser.hosts().size();
        waitingFor = new int[NUMBER_OF_HOSTS + 1];
        for (int i = 0; i < waitingFor.length; i++)
            waitingFor[i] = 1;

        waitingToBeSent = new ConcurrentLinkedDeque<>();
        mToSend = new ConcurrentLinkedDeque<>();
        mOnTheRoad = new ConcurrentHashMap<>();
        mToDeliver = new ConcurrentLinkedDeque<>();

        mSentInWindow = new AtomicInteger(0);
        plRT = new Thread(new PLReceivingThread());
        plST = new Thread(new PLSendingThread());
        plTOT = new Thread(new PLTimeoutThread());
        plRT.setDaemon(true);
        plST.setDaemon(true);
        plTOT.setDaemon(true);
        plST.start();
        plRT.start();
        plTOT.start();
    }

    private void timeout(Message m) {
        if(System.currentTimeMillis() > nextTimeOut) {
            timeoutInterval = (int) (timeoutInterval * MULTIPLICATE_WHEN_TIMEOUT);
            nextTimeOut = System.currentTimeMillis() + timeoutInterval;
        }
        mOnTheRoad.remove(m);
        mToSend.addLast(m);
    }

    private void updateTimeout(Long timeSent) {
        long sampleRtt = System.currentTimeMillis()-timeSent;
        estimatedRTT = (int) ((1-alpha) * estimatedRTT + alpha * sampleRtt);
        deviationRTT = (int) ((1-beta) * deviationRTT + beta * Math.abs(sampleRtt-estimatedRTT));
        timeoutInterval = estimatedRTT + 2*deviationRTT;
    }

    @Override
    public void close(){
        downLayer.close();
    }

    @Override
    public void deliveredFromBottom(Message m) {
        mToDeliver.addLast(m);
    }

    @Override
    public void sendFromTop(Message m) {
        waitingToBeSent.addLast(m);
    }

    private class PLSendingThread implements Runnable {
        @Override
        public void run() {
            while(true){
                while(!waitingToBeSent.isEmpty()){
                    mToSend.addLast(waitingToBeSent.pollFirst());
                }
                while(!mToSend.isEmpty()){
                    Message m = mToSend.pollFirst();
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        m.setTimeSent(System.currentTimeMillis());
                        mOnTheRoad.put(m,true);
                    }
                    downLayer.sendFromTop(m);
                }

            }
        }
    }

    private class PLReceivingThread implements Runnable {
        @Override
        public void run() {
            int i = 0;
            while(true){
                if(!mToDeliver.isEmpty()){
                    Message m = mToDeliver.pollFirst();
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        if(!mReceived.contains(m)) {
                            mReceived.add(m);
                            topLayer.deliveredFromBottom(m);
                        }
                        ackMessage(m);
                    }else if(m.getMessageType() == Message.MessageType.ACK){
                        mOnTheRoad.remove(m.mThatIsAcked());
                        mSentInWindow.decrementAndGet();
                        updateTimeout(m.getTimeSent());
                    }
                }
            }
        }
        private void ackMessage(Message m){
            Message ack = new Message(m.getDstIP(), m.getDstPort(), MY_ID, m.getSrcID(), m.getSrcIP(), m.getSrcPort(),
                    m.getSeqNumber(), Message.MessageType.ACK, "", m.getTimeSent());
            mToSend.addLast(ack);
        }
    }

    private class PLTimeoutThread implements Runnable{

        @Override
        public void run() {
            while(true){
                long time = System.currentTimeMillis();
                int to = timeoutInterval;
                Set<Message> s = new HashSet<>(mOnTheRoad.keySet());
                for(Message m : s){
                    if(time - m.getTimeSent() > to){
                        timeout(m);
                    }
                }
            }
        }
    }
}
