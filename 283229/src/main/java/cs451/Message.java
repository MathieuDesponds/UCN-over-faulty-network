package cs451;

import java.io.*;
import java.net.InetAddress;

public class Message implements Serializable {
    public enum MessageType {MESSAGE, ACK};

    private int srcID;
    private int seqNumber;
    private MessageType mt;
    private String payload;

    private String srcIP;
    private int srcPort;
    private transient String dstIP;
    private transient int dstPort;


    public Message(String srcIP, int srcPort, int srcID, String dstIP, int dstPort, int seqNumber, MessageType mt, String payload){
        this.srcIP = srcIP;
        this.srcPort = srcPort;
        this.srcID = srcID;
        this.dstIP = dstIP;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.mt = mt;
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

    public int getSrcID() {
        return srcID;
    }

    public MessageType getMessageType() {
        return mt;
    }

    public void setAddress(InetAddress dstaddress, int dstport) {
        this.dstIP = dstaddress.getHostName();
        this.dstPort = dstport;
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
}