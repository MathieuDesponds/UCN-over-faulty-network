package cs451.links;

import cs451.Message;

import java.lang.reflect.Array;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PerfectLink extends Link{
    private FairLossLink fll;
    private int waitingFor = 0; //for receiver
    private final int TIMEOUT = 5000;
    private final int WINDOW = 2;

    public PerfectLink(String ip, int port){
        fll = new FairLossLink(ip,port,TIMEOUT);
    }
    @Override
    public Message deliver() throws SocketTimeoutException {
        Message m = fll.deliver();
        Message ack = new Message(m.getSrcIP(), m.getSrcPort(), m.getSeqNumber(),"");
        System.out.print("ack "+m.getSeqNumber());
        fll.send(List.of(ack));
        return m;
    }
    
    @Override
    public void send(List<Message> lm) {
        fll.send(lm);
        HashSet<Integer> hs = new HashSet<Integer>(lm.stream().map(m -> m.getSeqNumber()).collect(Collectors.toSet()));
        Message m;
        try{
            while(true){
                m = fll.deliver();
                hs.remove(m.getSeqNumber());
            }
        }catch(SocketTimeoutException e) {

        }finally{
            //send(hs.stream().map(i -> lm.get(i)).collect(Collectors.toList()));
        }
    }

    @Override
    public void close(){
        fll.close();
        System.out.println("close");
    }
}
