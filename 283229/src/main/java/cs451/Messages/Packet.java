package cs451.Messages;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
        return "Message{" +
                "srcID=" + srcID +
                ", dstID=" + dstID +
                ", seqNumber=" + seqNumber +
                ", mt=" + mt +
                ", size =" + getSize() +
                '}';
    }


    public byte[] serializeToBytes() {
        //byte[] bytes = ByteBuffer.allocate(12).putInt(seqNumber).putInt(broadcasterID).putInt(payload.length()).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(seqNumber);
        outputStream.write(srcID);
        outputStream.write(dstID);
        //outputStream.writeBytes(timeSent);
        outputStream.write(getSize());
        for(BroadcastMessage bm : brcMessages){
            outputStream.writeBytes(bm.serializeToBytes());
        }
        return outputStream.toByteArray( );
    }

    public static Packet deserializeFromBytes(byte[] data) {
        int seqNumber = fromByteArray(data,0);
        int srcID = fromByteArray(data, 4);
        int dstID = fromByteArray(data, 8);

        int size = fromByteArray(data, 12+8);

        String payload = new String(Arrays.copyOfRange(data,8, data.length), StandardCharsets.UTF_8);;
        return new Packet(seqNumber, srcID,dstID, MessageType.MESSAGE, -1,-1);
    }

}
