package cs451;

import java.util.Arrays;

public class Message {
    private String srcIP;
    private int srcPort;
    private String dstIP;
    private int dstPort;
    private int seqNumber;
    private String payload;

    public Message(String srcIP, int srcPort, String dstIP, int dstPort, int seqNumber, String payload){
        this.srcIP = srcIP;
        this.srcPort = srcPort;
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.payload = payload;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public String getDstIP() {
        return dstIP;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public String getPayload() {
        return payload;
    }
}