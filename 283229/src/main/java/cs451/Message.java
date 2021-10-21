package cs451;

public class Message {
    public static enum MessageType {MESSSAGE, ACK};
    private String srcIP;
    private int srcPort;
    private int sndID;
    private String dstIP;
    private int dstPort;
    private int seqNumber;
    private MessageType mt;
    private String payload;


    public Message(String srcIP, int srcPort, int sndID, String dstIP, int dstPort, int seqNumber, MessageType mt, String payload){
        this.srcIP = srcIP;
        this.srcPort = srcPort;
        this.sndID = sndID;
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.mt = mt;
        this.payload = payload;
    }
    public Message(String dstIP, int dstPort,int sndID, int seqNumber,MessageType mt, String payload){
        this("", -1, sndID, dstIP, dstPort, seqNumber, mt, payload);
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

    public int getSndID() {
        return sndID;
    }

    public MessageType getMessageType() {
        return mt;
    }
}