package cs451;

import cs451.layers.Layer;
import cs451.layers.OutputLayer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Parser parser;
    private static Layer layer;

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

        layer = new OutputLayer(null, parser);

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

        List<BroadcastMessage> lm = instantiateMessages();

        System.out.println("Broadcasting and delivering messages...\n");

        send(lm);

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }

    private static List<BroadcastMessage> instantiateMessages() {
        List<BroadcastMessage> lm = new ArrayList<BroadcastMessage>();
        int configNbMessage = parser.configNbMessage();
        for(int i = 1; i<=configNbMessage; ++i) {
            lm.add(new BroadcastMessage(i, Message.MessageType.MESSAGE, ""));
        }
        return lm;
    }

    private static void close() {
        layer.close();
    }


    private static void send(List<BroadcastMessage> lm) {
        for(BroadcastMessage m :lm)
            layer.sendFromTop(m);
    }
}
