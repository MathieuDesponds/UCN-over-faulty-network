package cs451;

import java.io.*;

public class Message implements Serializable {
    public enum MessageType {MESSSAGE, ACK};

    private int sndID;
    private int seqNumber;
    private MessageType mt;
    private String payload;

    private transient String srcIP;
    private transient int srcPort;
    private transient String dstIP;
    private transient int dstPort;


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
            ObjectInputStream oi = new ObjectInputStream(bais) {
            }) {
            return (Message) oi.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}