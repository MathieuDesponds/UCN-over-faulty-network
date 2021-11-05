package cs451;

import java.io.*;
import java.util.Objects;

public class Message implements Serializable {
    public enum MessageType {MESSAGE, ACK};

    protected int srcID;
    protected int dstID;
    protected int broadcasterID;
    protected int seqNumber;
    protected MessageType mt;
    protected String payload;
    protected long timeSent;


    public Message(int srcID, int dstID, int broadcasterID, int seqNumber, MessageType mt, String payload, long timeSent){
        this.srcID = srcID;
        this.dstID = dstID;
        this.broadcasterID = broadcasterID;
        this.seqNumber = seqNumber;
        this.mt = mt;
        this.timeSent = timeSent;
        this.payload = payload;
    }
    public Message(int srcID, int dstID, int broadcasterID, int seqNumber, MessageType mt, String payload){
        this(srcID,dstID,broadcasterID,seqNumber,mt,payload, -1);
    }
    public Message(int srcID, int dstID, int seqNumber, MessageType mt, String payload){
        this(srcID,dstID,-1, seqNumber,mt,payload, -1);
    }

    private Message(int srcID, int dstID, int seqNumber, MessageType mt){
        this(srcID,dstID,-1, seqNumber,mt,"", -1);
    }

    public Message(int seqNumber, MessageType mt, String payload) {
        this(-1,-1,- 1, seqNumber,mt,payload, -1);
    }
    public Message(Message m) {
        this(m.srcID,m.dstID,m.broadcasterID,m.seqNumber,m.mt,m.payload, m.timeSent);
    }

    //*************************************************************//

    public Message getAckedMessageToHash() {
        return new Message(this.dstID, this.srcID, this.seqNumber, MessageType.MESSAGE);
    }
    public Message getAckingMessage() {
        return new Message(this.dstID, this.srcID,this.broadcasterID, this.seqNumber, MessageType.ACK, "", this.timeSent);
    }


    public void setClientServer(int srcID, int dstID) {
        this.srcID = srcID;
        this.dstID = dstID;
    }


    //*************************************************************//

    //*************************************************************//

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

    public long getTimeSent() {
        return timeSent;
    }

    public int getBroadcasterID() {
        return broadcasterID;
    }

    public void setBroadcasterID(int broadcasterID) {
        this.broadcasterID = broadcasterID;
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
                broadcasterID == message.broadcasterID &&
                seqNumber == message.seqNumber &&
                mt == message.mt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcID, dstID, broadcasterID, seqNumber, mt);
    }


    @Override
    public String toString() {
        return "Message{" +
                "srcID=" + srcID +
                ", dstID=" + dstID +
                ", brdID=" + broadcasterID +
                ", seqNumber=" + seqNumber +
                ", mt=" + mt +
                '}';
    }
}