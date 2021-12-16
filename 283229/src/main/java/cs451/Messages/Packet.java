package cs451.Messages;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Packet extends Message {
    private static int nextSeqNumber = 1;
    public enum MessageType {MESSAGE, ACK};
    private MessageType mt;
    private transient long timeSent;
    private int srcID;
    private int dstID;
    private ConcurrentLinkedDeque<BroadcastMessage> brcMessages;
    private int byteSize = 0;
    private long timeCreated;

    protected Packet( int seqNumber, int srcID, int dstID, MessageType mt, long timeSent, long timeCreated) {
        super(seqNumber);
        this.srcID = srcID;
        this.dstID = dstID;
        this.mt = mt;
        this.timeSent = timeSent;
        if(mt == MessageType.MESSAGE)
            brcMessages = new ConcurrentLinkedDeque<>();
        else
            brcMessages = null;
        this.timeCreated = timeCreated;
    }
    public Packet(int srcID, int dstID, MessageType mt, long timeCreated) {
        this( nextSeqNumber++, srcID,dstID,mt,-1, timeCreated);
    }

    public Packet getAckedPacketToHash() {
        return new Packet(this.seqNumber, this.dstID, this.srcID, MessageType.MESSAGE,-1,-1);
    }
    public Packet getAckingPacket() {
        return new Packet(this.seqNumber, this.dstID, this.srcID, MessageType.ACK, this.timeSent,-1);
    }

    public int getDstID() {
        return dstID;
    }

    public int getSrcID() {
        return srcID;
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
        return mt == MessageType.MESSAGE? brcMessages.size(): 0;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void addBM(BroadcastMessage bm){
        brcMessages.addLast(bm);
        byteSize += bm.getByteSize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet)) return false;
        Packet packet = (Packet) o;
        return srcID == packet.srcID &&
                dstID == packet.dstID &&
                seqNumber == packet.seqNumber &&
                mt == packet.mt;
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
                ", size =" + getSize() +
                '}';
    }
    public byte[] serializeToBytes() {
        int totalSize;
        if(mt == MessageType.ACK) {
            totalSize = 13;
            return ByteBuffer.allocate(totalSize).putInt(seqNumber).putInt(srcID).putInt(dstID).put((byte)mt.ordinal()).array();
        }else {
            totalSize = 17 + byteSize;//28+8*brc.length
            ByteBuffer bb = ByteBuffer.allocate(totalSize)
                    .putInt(seqNumber).putInt(srcID).putInt(dstID).put((byte) mt.ordinal()).putInt(getSize());
            for (BroadcastMessage bm : brcMessages) {
                bb.put(((BroadcastMessageSent) bm).getBytes());
            }
            return bb.array();
        }
    }

    public static Packet deserializeFromBytes(byte[] data) {
        int seqNumber = intFromByteArray(data,0);
        int srcID = intFromByteArray(data, 4);
        int dstID = intFromByteArray(data, 8);
        MessageType mt = data[12]==0?MessageType.MESSAGE:MessageType.ACK;
        Packet pkt =  new Packet(seqNumber, srcID,dstID, mt, -1,-1);
        if(mt == MessageType.MESSAGE) {
            int size = intFromByteArray(data, 13);
            int startPoint = 17;
            for (int i = 0; i < size; i++) {
                BroadcastMessage bm = BroadcastMessage.deserializeFromBytes(data, startPoint);
                startPoint += bm.getByteSize();
                pkt.addBM(bm);
            }
        }
        return pkt;
    }
}
