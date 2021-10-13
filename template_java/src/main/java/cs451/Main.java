package cs451;

import cs451.links.FairLossLink;
import cs451.links.Link;
import cs451.links.PerfectLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Parser parser;
    private static Link link;            ;
    private static final int TIMEOUT = 8000; //TOdo remove it
    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        close();
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
        parser = new Parser(args);
        parser.parse();
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

        Host hostToSend = getHostToSendTo();
        Host me = getMe();

        // Tell what is the link
        link = new PerfectLink(me.getIp(), me.getPort(), parser.output());

        System.out.println("Broadcasting and delivering messages...\n");

        //See if we are the host to send to
        if(parser.myId() == hostToSend.getId()){
            receiver();
        }else{
            sender(parser.configNbMessage(), hostToSend);
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
        link.close();
    }

    private static void receiver() {
        Message m;
        try{
            while(true){
                m = link.deliver();
            }
        }catch(SocketTimeoutException e){
            System.out.println("Timeout");
        }
    }

    private static void sender(int configNbMessage, Host hostToSend) {
        //Preparation of the sended messages
        List<Message> lm = new ArrayList<Message>();
        for(int i = 0; i<configNbMessage; ++i) {
            lm.add(new Message(hostToSend.getIp(), hostToSend.getPort(), i, "AAAA" + i));
        }
        //Sending messages
        link.send(lm);
    }

    private static Host getHostToSendTo() {
        int idToSend = parser.configIdToSend();
        for(Host h : parser.hosts()){
            if(h.getId()==idToSend)
                return h;
        }
        return null;
    }
    private static Host getMe() {
        int myId = parser.myId();
        for(Host h : parser.hosts()){
            if(h.getId()==myId)
                return h;
        }
        return null;
    }
}
