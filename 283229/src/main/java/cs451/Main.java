package cs451;

import cs451.layers.Layer;
import cs451.layers.OutputLayer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Parser parser;
    private static Layer layer;
    private static final int TIMEOUT_SENDER =500;
    private static final int TIMEOUT_RECEIVER = 50000;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        //write/flush output file if necessary
        System.out.println("Writing output.");
        close();
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
        int myId = me.getId();

        // Tell what is the link


        System.out.println("Broadcasting and delivering messages...\n");

        //See if we are the host to send to
        if(myId == hostToSend.getId()){
            layer = new OutputLayer(null, me.getIp(), me.getPort(), parser);
        }else{
            layer = new OutputLayer(null, me.getIp(), me.getPort(), parser);
            sender(parser.configNbMessage(), hostToSend, myId);
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    private static void close() {
        layer.close();
    }


    private static void sender(int configNbMessage, Host hostToSend, int myID) {
        //Preparation of the sended messages
        List<Message> lm = new ArrayList<Message>();
        Host me = getMe();
        String myIp = me.getIp();
        int myPort = me.getPort();
        for(int i = 1; i<=configNbMessage; ++i) {
            lm.add(new Message(myIp, myPort, myID, hostToSend.getIp(), hostToSend.getPort(), i, Message.MessageType.MESSAGE, "AAAA" + i));
        }
        //Sending messages

        for(Message m :lm)
            layer.sendFromTop(m);
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
