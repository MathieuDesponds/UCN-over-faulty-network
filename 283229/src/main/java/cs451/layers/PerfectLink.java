package cs451.layers;

import cs451.Message;
import cs451.Parser;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink extends Layer{
    private final int MY_ID;
    //Go-Back-N
    private final int WINDOW = 2;
    private final int NUMBER_OF_HOSTS;
    private int base = 1; //Seq number of the base --> seq n is at list(n-1)
    private AtomicInteger mSentInWindow;
    private int [] waitingFor; //for receiver

    //TIMEOUT
    private int estimatedRTT = 500;
    private int deviationRTT = 125;
    private int timeoutInterval = 500;
    private final int TIMEOUT_INTERVAL_MAX = 2500;
    private final double alpha = 0.25; //Recommended 0.125
    private final double beta = 0.25;
    private ConcurrentLinkedDeque<Long> timeouts;

    //Thread
    Thread plRT,plST,plTOT;
    private ConcurrentLinkedDeque<Message> mToSend;
    private ConcurrentLinkedDeque<Message> waitingToBeSent;
    private ConcurrentLinkedDeque<Message> mToDeliver;
    private ConcurrentLinkedDeque<Message> mOnTheRoad;
    private AtomicBoolean stopSending;

    public PerfectLink(Layer topLayer, String ip, int port, Parser parser) {
        super.setTopLayer(topLayer);
        super.setDownLayer(new FairLossLink(this, ip, port));

        this.MY_ID = parser.myId();
        this.NUMBER_OF_HOSTS = parser.hosts().size();
        waitingFor = new int[NUMBER_OF_HOSTS + 1];
        for (int i = 0; i < waitingFor.length; i++)
            waitingFor[i] = 1;

        timeouts = new ConcurrentLinkedDeque<>();
        waitingToBeSent = new ConcurrentLinkedDeque<>();
        mToSend = new ConcurrentLinkedDeque<>();
        mOnTheRoad = new ConcurrentLinkedDeque<>();
        mToDeliver = new ConcurrentLinkedDeque<>();

        mSentInWindow = new AtomicInteger(0);
        stopSending = new AtomicBoolean(false);
        plRT = new Thread(new PLReceivingThread());
        plST = new Thread(new PLSendingThread(stopSending));
        plTOT = new Thread(new PLTimeoutThread());
        plRT.setDaemon(true);
        plST.setDaemon(true);
        plTOT.setDaemon(true);
        plST.start();
        plRT.start();
        plTOT.start();
    }

    private void timeout() {
        timeoutInterval = timeoutInterval*2;
        timeouts.clear();
        stopSending.set(true);
        for (int i = 0 ; i < mSentInWindow.get() ;i++) {
            mToSend.addFirst(mOnTheRoad.pollLast());
        }
        stopSending.set(false);
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
        private AtomicBoolean stopSending;
        private PLSendingThread(AtomicBoolean stopSending){
            this.stopSending = stopSending;
        }
        @Override
        public void run() {
            while(true){
                while(mSentInWindow.get() < WINDOW && !waitingToBeSent.isEmpty()){
                    mToSend.addLast(waitingToBeSent.pollFirst());
                    mSentInWindow.incrementAndGet();
                }
                while(!stopSending.get() && !mToSend.isEmpty()){
                    Message m = mToSend.pollFirst();
                    downLayer.sendFromTop(m);
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        mOnTheRoad.addLast(m);
                        timeouts.addLast(System.currentTimeMillis());
                    }
                }

            }
        }
    }

    private class PLReceivingThread implements Runnable {
        @Override
        public void run() {
            while(true){
                if(!mToDeliver.isEmpty()){
                    Message m = mToDeliver.pollFirst();
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        if(m.getSeqNumber() < waitingFor[m.getSrcID()]) { //ReAcking because ack got lost
                            ackMessage(m);
                        }else if(m.getSeqNumber() == waitingFor[m.getSrcID()]) {
                            waitingFor[m.getSrcID()]++;
                            topLayer.deliveredFromBottom(m);
                            //send ack
                            ackMessage(m);
                        }
                    }else if(m.getMessageType() == Message.MessageType.ACK){
                        if (m.getSeqNumber() == base) {
                            mOnTheRoad.removeFirst();
                            mSentInWindow.decrementAndGet();
                            updateTimeout(timeouts.getFirst());
                            timeouts.removeFirst();
                            base++;
                        }
                    }
                }
            }
        }
        private void ackMessage(Message m){
            Message ack = new Message(m.getDstIP(), m.getDstPort(), MY_ID, m.getSrcIP(), m.getSrcPort(),
                    m.getSeqNumber(), Message.MessageType.ACK, "");
            mToSend.addLast(ack);
        }
    }

    private class PLTimeoutThread implements Runnable{

        @Override
        public void run() {
            while(true){
                checkTimeoutGoBackN();
            }
        }
        private void checkTimeoutGoBackN() {
            try {
                if (!timeouts.isEmpty() && System.currentTimeMillis() - timeouts.getFirst() > timeoutInterval)
                    timeout();
            }catch(NoSuchElementException e){}
        }
    }
}
