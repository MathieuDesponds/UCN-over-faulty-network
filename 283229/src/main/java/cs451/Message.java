package cs451;

import java.io.*;
import java.net.InetAddress;
import java.util.Objects;

public class Message implements Serializable {

    public Message(Message m) {
        this(m.srcIP,m.srcPort,m.srcID,m.dstID,m.dstIP,m.dstPort,m.seqNumber,m.mt,m.payload, m.timeSent);
    }

    public Message mThatIsAcked() {
        return new Message(this.dstID, this.srcID, this.seqNumber, MessageType.MESSAGE);
    }

    public void setClientServer(String srcIp, int srcPort, int srcID, String dstIP, int dstPort, int dstID) {
        this.srcIP = srcIp; this.srcPort = srcPort; this.srcID = srcID;
        this.dstIP = dstIP; this.dstPort = dstPort; this.dstID = dstID;
    }

    public enum MessageType {MESSAGE, ACK};

    private int srcID;
    private int dstID;
    private int seqNumber;
    private MessageType mt;
    private String payload;
    private long timeSent;

    private String srcIP;
    private int srcPort;
    private transient String dstIP;
    private transient int dstPort;

    public Message(String srcIP, int srcPort, int srcID, int dstID, String dstIP, int dstPort, int seqNumber,
                   MessageType mt, String payload, long timeSent){
        this.srcIP = srcIP;
        this.srcPort = srcPort;
        this.srcID = srcID;
        this.dstID = dstID;
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.mt = mt;
        this.timeSent = timeSent;
        this.payload = payload;
        this.timeSent = timeSent;
    }
    public Message(String srcIP, int srcPort, int srcID, int dstID, String dstIP, int dstPort, int seqNumber, MessageType mt, String payload){
        this(srcIP,srcPort,srcID,dstID,dstIP,dstPort,seqNumber,mt,payload, -1);
    }

    private Message(int srcID, int dstID, int seqNumber, MessageType mt){
        this("",-1,srcID,dstID,"",-1,seqNumber,mt,"");
    }

    public Message(int seqNumber, MessageType mt, String payload) {
        this("",-1,-1,-1,"",-1,seqNumber,mt,payload);
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

    public int getSrcID() {
        return srcID;
    }

    public int getDstID() {
        return dstID;
    }

    public MessageType getMessageType() {
        return mt;
    }

    public void setAddress(InetAddress dstaddress, int dstport) {
        this.dstIP = dstaddress.getHostName();
        this.dstPort = dstport;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public  byte[] serializeToBytes(){
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)){
            oos.writeObject(this);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Message deserializeFromBytes(byte[] data){
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream oi = new ObjectInputStream(bais);) {
            Message m = (Message) oi.readObject();
            return m;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return srcID == message.srcID &&
                dstID == message.dstID &&
                seqNumber == message.seqNumber &&
                mt == message.mt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcID, dstID, seqNumber, mt);
    }

    @Override
    public String toString() {
        return "Message{" +
                "srcID=" + srcID +
                ", dstID=" + dstID +
                ", seqNumber=" + seqNumber +
                ", mt=" + mt +
                ", timeSent=" + timeSent +
                '}';
    }
}