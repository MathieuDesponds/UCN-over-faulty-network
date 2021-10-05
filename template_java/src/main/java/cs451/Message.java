package cs451;

import java.util.Arrays;

public class Message {
    private int srcIP; private int srcPort;
    private int dstIP; private int dstPort;
    private int seqNumber;
    private byte[] payload;

    public Message(int srcIP, int srcPort, int dstIP, int dstPort, int seqNumber, byte[] payload){
        this.srcIP = srcIP;
        this.srcPort = srcPort;
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.payload = Arrays.copyOf(payload,payload.length);
    }

    public int getSrcIP() {
        return srcIP;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstIP() {
        return dstIP;
    }

    public int getDstPort() {
        return dstPort;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public byte[] getPayload() {
        return payload;
    }
}
