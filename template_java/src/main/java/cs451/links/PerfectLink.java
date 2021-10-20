package cs451.links;

import cs451.Message;

import java.net.SocketTimeoutException;
import java.util.ArrayDeque;

public class PerfectLink extends Link{
    private FairLossLink fll;

    //Go-Back-N
    private final int WINDOW = 10;
    private final int NUMBER_OF_HOSTS;
    private int base = 1; //Seq number of the base --> seq n is at list(n-1)
    private int nextSend = 1;
    private long sendTime = 0;
    private int [] waitingFor; //for receiver
    private ArrayDeque<Message> windowMessages;

    //TIMEOUT
    private int estimatedRTT = 500;
    private int deviationRTT = 125;
    private int timeoutInterval = 500;
    private final int TIMEOUT_INTERVAL_MAX = 2500;
    private final double alpha = 0.25; //Recommended 0.125
    private final double beta = 0.25;
    private ArrayDeque<Long> timeouts;

    public PerfectLink(String ip, int port, int timeout, int numberOfHosts){
        fll = new FairLossLink(ip,port,timeout);
        this.NUMBER_OF_HOSTS = numberOfHosts;
        waitingFor = new int [NUMBER_OF_HOSTS+1];
        for(int i=0; i<waitingFor.length ; i++)
            waitingFor[i] = 1;
        windowMessages = new ArrayDeque<Message>();
        timeouts = new ArrayDeque<>();
    }

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
    
    @Override
    public void send(Message m) {
        if(windowMessages.size() < WINDOW){
            fll.send(m);
            windowMessages.addLast(m);
            timeouts.addLast(System.currentTimeMillis());
            checkTimeoutGoBackN();
        }else {
            boolean accepted = false;
            while(!accepted) {
                checkTimeoutGoBackN();
                try {
                    Message ack = fll.deliver();
                    if (ack != null && ack.getSeqNumber() == base) {
                        accepted = true;
                        //System.out.println("ack " + m.getSeqNumber());
                        windowMessages.removeFirst();
                        updateTimeout(timeouts.getFirst());
                        timeouts.removeFirst();
                        base++;
                        fll.send(m);
                        windowMessages.addLast(m);
                        timeouts.addLast(System.currentTimeMillis());
                    }
                } catch (SocketTimeoutException e) {}
            }
        }
    }

    private void timeout() {
        timeoutInterval = Math.min(timeoutInterval*2, TIMEOUT_INTERVAL_MAX);
        timeouts.clear();
        for (Message me : windowMessages) {
            fll.send(me);
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
        //System.out.println("close");
    }
}
