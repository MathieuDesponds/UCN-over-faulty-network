package cs451.layers;

import cs451.Message;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PerfectLink extends Layer{
    private FairLossLink fll;
    private Layer topLayer;
    //Go-Back-N
    private final int WINDOW = 10;
    private final int NUMBER_OF_HOSTS;
    private int base = 1; //Seq number of the base --> seq n is at list(n-1)
    private int nextSend = 1;
    private long sendTime = 0;
    private int [] waitingFor; //for receiver

    //TIMEOUT
    private int estimatedRTT = 500;
    private int deviationRTT = 125;
    private int timeoutInterval = 500;
    private final int TIMEOUT_INTERVAL_MAX = 2500;
    private final double alpha = 0.25; //Recommended 0.125
    private final double beta = 0.25;
    private ArrayDeque<Long> timeouts;

    //Thread
    private ConcurrentLinkedDeque<Message> mToSend;
    private ConcurrentLinkedDeque<Message> waitingToBeSent;
    private ConcurrentLinkedDeque<Message> windowMessages;
    private ConcurrentLinkedDeque<Message> messageToDeliver;

    public PerfectLink(Layer topLayer, String ip, int port, int timeout, int numberOfHosts){
        this.topLayer = topLayer;
        fll = new FairLossLink(this,ip,port);
        this.NUMBER_OF_HOSTS = numberOfHosts;
        waitingFor = new int [NUMBER_OF_HOSTS+1];
        for(int i=0; i<waitingFor.length ; i++)
            waitingFor[i] = 1;
        windowMessages = new ConcurrentLinkedDeque<Message>();
        timeouts = new ArrayDeque<>();

        mToSend = new ConcurrentLinkedDeque<>();
        messageToDeliver = new ConcurrentLinkedDeque<>();
    }
/*
    @Override
    public Message deliver() throws SocketTimeoutException {
        Message m = fll.deliver();
        if(m != null && m.getSeqNumber() < waitingFor[m.getSndID()]) { //ReAcking
            fll.send(new Message(m.getSrcIP(), m.getSrcPort(),m.getSndID(), m.getSeqNumber(), ""));
        } else if(m != null && m.getSeqNumber() == waitingFor[m.getSndID()]) {
            fll.send(new Message(m.getSrcIP(), m.getSrcPort(),m.getSndID(), m.getSeqNumber(), ""));
            waitingFor[m.getSndID()]++;
            return m;
        }
        return null;
    }
    
*/
    public void send(Message m) {
        waitingToBeSent.addLast(m);
    }

    private void timeout() {
        timeoutInterval = Math.min(timeoutInterval*2, TIMEOUT_INTERVAL_MAX);
        timeouts.clear();
        for (Message me : windowMessages) {
            //fll.send(me);
            timeouts.addLast(System.currentTimeMillis());
        }
    }

    private void updateTimeout(Long timeSent) {
        long sampleRtt = System.currentTimeMillis()-timeSent;
        estimatedRTT = (int) ((1-alpha) * estimatedRTT + alpha * sampleRtt);
        deviationRTT = (int) ((1-beta) * deviationRTT + beta * Math.abs(sampleRtt-estimatedRTT));
        timeoutInterval = estimatedRTT + 2*deviationRTT;
        fll.setTimeOut(timeoutInterval);
    }

    private void checkTimeoutGoBackN() {
        if(System.currentTimeMillis()-timeouts.getFirst() > timeoutInterval)
            timeout();
    }

    @Override
    public void close(){
        fll.close();
    }

    @Override
    public void deliveredFromBottom(Message m) {
        messageToDeliver.addLast(m);
    }

    @Override
    public void sendFromTop(Message m) {
        mToSend.addLast(m);
    }

    private class PLSendingThread implements Runnable {

        @Override
        public void run() {
            while(true){
                while(windowMessages.size() < WINDOW){
                    mToSend.addLast(waitingToBeSent.pollFirst());
                }
                while(!mToSend.isEmpty()){
                    Message m = mToSend.pollFirst();
                    //fll.send(m);
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        windowMessages.addLast(m);
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
                if(!messageToDeliver.isEmpty()){
                    Message m = messageToDeliver.pollFirst();
                    if(m.getMessageType() == Message.MessageType.MESSAGE){
                        topLayer.deliveredFromBottom(m);
                    }else if(m.getMessageType() == Message.MessageType.ACK){
                        if (m.getSeqNumber() == base) {
                            //System.out.println("ack " + m.getSeqNumber());
                            windowMessages.removeFirst();
                            updateTimeout(timeouts.getFirst());
                            timeouts.removeFirst();
                            base++;
                        }
                    }
                }
            }
        }
    }
}
