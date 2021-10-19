package cs451.links;

import cs451.Message;

import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.concurrent.TimeoutException;

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
        windowMessages = new ArrayDeque<Message>();
    }

    @Override
    public Message deliver() throws SocketTimeoutException {
        Message m = fll.deliver();
        if(m.getSeqNumber() == waitingFor[m.getSndID()]) {
            fll.send(new Message(m.getSrcIP(), m.getSrcPort(),m.getSndID(), m.getSeqNumber(), ""));
            waitingFor[m.getSndID()]++;
            return m;
        }
        //return new Message(m.getSrcIP(), m.getSrcPort(), m.getSeqNumber(), "Nothing");
        return null;
    }
    
    @Override
    public void send(Message m) {
        if(windowMessages.size() < WINDOW){
            fll.send(m);
            windowMessages.addLast(m);
        }else {
            boolean accepted = false;
            while(!accepted)
                try {
                    Message ack = fll.deliver();
                    if (ack.getSeqNumber() == base) {
                        accepted = true;
                        System.out.println("ack "+m.getSeqNumber());
                        windowMessages.removeFirst();
                        base++;
                        fll.send(m);
                        windowMessages.addLast(m);
                    }
                } catch (SocketTimeoutException e) {
                    for (Message me : windowMessages) {
                        fll.send(me);
                    }
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
