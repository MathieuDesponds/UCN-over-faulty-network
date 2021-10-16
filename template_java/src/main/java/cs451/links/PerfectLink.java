package cs451.links;

import cs451.Message;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class PerfectLink extends Link{
    private FairLossLink fll;

    //Go-Back-N
    private final int WINDOW = 10;
    private int base = 0;
    private int nextSend = 0;
    private long sendTime = 0;
    private int waitingFor = 0; //for receiver
    private final int TIMEOUT = 5000;

    public PerfectLink(String ip, int port, int timeout){
        fll = new FairLossLink(ip,port,timeout);
    }

    @Override
    public Message deliver() throws SocketTimeoutException {
        Message m = fll.deliver();
        if(m.getSeqNumber() == waitingFor) {
            fll.send(List.of(new Message(m.getSrcIP(), m.getSrcPort(),m.getSndID(), m.getSeqNumber(), "")));
            waitingFor++;
            return m;
        }
        //return new Message(m.getSrcIP(), m.getSrcPort(), m.getSeqNumber(), "Nothing");
        return null;
    }
    
    @Override
    public void send(List<Message> lm) {
        int size = lm.size();
        while(base < size) {
            int to = Math.min(size, base + WINDOW);
            fll.send(lm.subList(nextSend, to));
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
                System.out.println("TIMEOUT");
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
