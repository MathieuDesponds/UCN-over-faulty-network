package cs451;

import cs451.links.FairLossLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Main {
    private static OutputWriter ow;
    private static FairLossLink fll;
    private static final int TIMEOUT = 8000;
    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();
        ow= new OutputWriter(parser.output());
        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        Host hostToSend = getHostToSendTo(parser);
        Host me = getMe(parser);
        System.out.println("Broadcasting and delivering messages...\n");

        //See if we are the host to send to
        if(parser.myId() == hostToSend.getId()){
            receiver(me);
        }else{
            sender(me, parser.configNbMessage(), hostToSend);
        }

        close();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    private static void close() {
        fll.close();
        ow.write();
    }

    private static void receiver(Host me) {
        fll = new FairLossLink(me.getIp(), me.getPort(),TIMEOUT);
        System.out.println(me.getIp()+" "+me.getPort());
        Message m;
        try{
            while(true){
                m = fll.deliver();
                ow.addReceive(m);
                System.out.println("Received");
            }
        }catch(SocketTimeoutException e){
            System.out.println("Timeout");
        }
    }

    private static void sender(Host me, int configNbMessage, Host hostToSend) {
        fll= new FairLossLink(me.getIp(), me.getPort());
        System.out.println(me.getIp()+" "+me.getPort());
        for(int i = 0; i<configNbMessage; ++i){
            Message m = new Message(hostToSend.getIp(),hostToSend.getPort(),i,"AAAA"+i);
            fll.send(m);
            ow.addBroadcast(m);
        }
    }

    private static Host getHostToSendTo(Parser parser) {
        int idToSend = parser.configIdToSend();
        for(Host h : parser.hosts()){
            if(h.getId()==idToSend)
                return h;
        }
        return null;
    }
    private static Host getMe(Parser parser) {
        int myId = parser.myId();
        for(Host h : parser.hosts()){
            if(h.getId()==myId)
                return h;
        }
        return null;
    }
}
