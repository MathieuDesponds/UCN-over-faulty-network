package cs451.Messages;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Packet extends Message {
    private static int nextSeqNumber = 1;


    public enum MessageType {MESSAGE, ACK};
    private MessageType mt;
    private long timeSent;
    private int srcID;
    private int dstID;
    private ConcurrentLinkedDeque<BroadcastMessage> brcMessages;

    private transient long timeCreated;

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

    public long getTimeCreated() {
        return timeCreated;
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
                seqNumber == packet.seqNumber &&
                mt == packet.mt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcID, dstID, seqNumber, mt);
    }

    @Override
    public String toString() {
        String s = "Packet {" +
                "srcID=" + srcID +
                ", dstID=" + dstID +
                ", seqNumber=" + seqNumber +
                ", mt=" + mt +
                ", size =" + getSize() +
                ", ts =" + timeSent +
                "} \n";
        for(BroadcastMessage bm : brcMessages)
            s+= bm.toString()+"\n";
        return s;
    }


    public byte[] serializeToBytes() {
        int totalsize = 25 + 8* brcMessages.size();//28+8*brc.length
        ByteBuffer bb = ByteBuffer.allocate(totalsize).putInt(seqNumber).putInt(srcID).putInt(dstID).put((byte)mt.ordinal())
                .putLong(timeSent).putInt(getSize());
        for(BroadcastMessage bm : brcMessages){
            bb.put(bm.serializeToBytes());
        }
        System.out.println(bb.array().length);
        return bb.array();
    }

    public static Packet deserializeFromBytes(byte[] data) {
        int seqNumber = intFromByteArray(data,0);
        int srcID = intFromByteArray(data, 4);
        int dstID = intFromByteArray(data, 8);
        MessageType mt = data[12]==0?MessageType.MESSAGE:MessageType.ACK;
        long timeSent = longFromByteArray(data, 13);
        int size = intFromByteArray(data, 21);
        Packet pkt =  new Packet(seqNumber, srcID,dstID, MessageType.MESSAGE, timeSent,-1);

        for (int i = 0; i<size; i++){
            pkt.addBM(BroadcastMessage.deserializeFromBytes(data, 25+8*i));
        }
        return pkt;
    }

}
