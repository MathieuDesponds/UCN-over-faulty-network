package cs451.links;

import cs451.Message;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class PerfectLink extends Link{
    private FairLossLink fll;

    //Go-Back-N
    private final int WINDOW = 2;
    private final int NUMBER_OF_HOSTS;
    private int base = 1; //Seq number of the base --> seq n is at list(n-1)
    private int nextSend = 1;
    private long sendTime = 0;
    private int [] waitingFor; //for receiver

    //TIMEOUT
    private final int TIMEOUT = 1000;
    private int estimatedRTT;
    private int deviationRTT;
    private int timeoutInterval = 1000;
    private double alpha = 0.125;
    private double beta = 0.25;

    public PerfectLink(String ip, int port, int timeout, int numberOfHosts){
        fll = new FairLossLink(ip,port,timeout);
        this.NUMBER_OF_HOSTS = numberOfHosts;
        waitingFor = new int [NUMBER_OF_HOSTS+1];
        for(int i=0; i<waitingFor.length ; i++)
            waitingFor[i] = 1;
    }

    @Override
    public Message deliver() throws SocketTimeoutException {
        Message m = fll.deliver();
        if(m.getSeqNumber() == waitingFor[m.getSndID()]) {
            fll.send(List.of(new Message(m.getSrcIP(), m.getSrcPort(),m.getSndID(), m.getSeqNumber(), "")));
            waitingFor[m.getSndID()]++;
            return m;
        }
        //return new Message(m.getSrcIP(), m.getSrcPort(), m.getSeqNumber(), "Nothing");
        return null;
    }
    
    //@Override
    public void send(List<Message> lm) {
        int size = lm.size();
        while(base < size+1) {
            int to = Math.min(size+1, base + WINDOW );
            for(Message m : lm.subList(nextSend-1, to-1)) {
                fll.send(m);
            }
            setTimer();
            nextSend = to;
            Message m;
            try {
                m = fll.deliver();
                if (m.getSeqNumber() == base) {
                    //System.out.println("ack "+m.getSeqNumber());
                    base++;
                }
                checkTimeout();
            } catch (SocketTimeoutException | TimeoutException e) {
                //System.out.println("TIMEOUT");
                nextSend = base;
            }
        }
    }

    private void checkTimeout() throws TimeoutException {
        long currentTime = System.currentTimeMillis();
        if(sendTime + TIMEOUT < currentTime)
            throw new TimeoutException();
    }

    private void setTimer() {
        sendTime = System.currentTimeMillis();
    }

    @Override
    public void close(){
        fll.close();
        System.out.println("close");
    }
}
