package cs451.Messages;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Packet extends Message {

    public enum MessageType {MESSAGE, ACK};
    private MessageType mt;
    private long timeSent;
    private int srcID;
    private int dstID;
    private ConcurrentLinkedDeque<BroadcastMessage> brcMessages;

    protected Packet(int broadcasterID, int seqNumber, int srcID, int dstID, MessageType mt, long timeSent) {
        super(broadcasterID, seqNumber);
        this.srcID = srcID;
        this.dstID = dstID;
        this.mt = mt;
        this.timeSent = timeSent;
        brcMessages = new ConcurrentLinkedDeque<>();
    }
    public Packet(int broadcasterID, int srcID, int dstID, MessageType mt) {
        this(broadcasterID, -1, srcID,dstID,mt,-1);
    }

    public void setClientServer(int srcID, int dstID) {
        this.srcID = srcID;
        this.dstID = dstID;
    }
    public Packet getAckedPacketToHash() {
        return new Packet(this.broadcasterID, this.seqNumber, this.dstID, this.srcID, MessageType.MESSAGE,-1);
    }
    public Packet getAckingPacket() {
        return new Packet(this.broadcasterID, this.seqNumber, this.dstID, this.srcID, MessageType.ACK, this.timeSent);
    }

    public int getSrcID() {
        return srcID;
    }

    public int getDstID() {
        return dstID;
    }

    public ConcurrentLinkedDeque<BroadcastMessage> getBrcMessages() {
        return brcMessages;
    }

    public MessageType getMessageType() {
        return mt;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public int getSize() {
        return brcMessages.size();
    }

    public void addBM(BroadcastMessage bm){
        brcMessages.addLast(bm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet)) return false;
        Packet packet = (Packet) o;
        return srcID == packet.srcID &&
                dstID == packet.dstID &&
                broadcasterID == packet.broadcasterID &&
                seqNumber == packet.seqNumber &&
                mt == packet.mt;
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
