package cs451.Messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class BroadcastMessage extends Message {
    private int broadcasterID;
    private String payload;
    private transient int dstId;


    public BroadcastMessage(int seqNumber, int broadcasterID, String payload) {
        super(seqNumber);
        this.broadcasterID = broadcasterID;
        this.payload = payload;
    }

    public BroadcastMessage(int seqNumber, String payload) {
        this(seqNumber, -1, payload);
    }

    public BroadcastMessage(BroadcastMessage bm, int dstId) {
        super(bm.getSeqNumber());
        this.broadcasterID = bm.getBroadcasterID();
        this.dstId = dstId;
    }

    public int getDstId() {
        return dstId;
    }

    public int getBroadcasterID() {
        return broadcasterID;
    }

    public void setBroadcasterID(int broadcasterID) {
        this.broadcasterID = broadcasterID;
    }


    @Override
    public int hashCode() {
        return Objects.hash(broadcasterID, seqNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BroadcastMessage)) return false;
        BroadcastMessage message = (BroadcastMessage) o;
        return broadcasterID == message.broadcasterID &&
                seqNumber == message.seqNumber;
    }

    @Override
    public String toString() {
        return "BM {" +
                "brdID=" + broadcasterID +
                ", seqNumber=" + seqNumber +
                '}';
    }

    public byte[] serializeToBytes() {
        byte[] payloadByte = payload.getBytes();
        byte[] bytes = ByteBuffer.allocate(8+payloadByte.length).putInt(seqNumber).putInt(broadcasterID)
                .put(payload.getBytes()).array();
        return bytes;
    }

    public static BroadcastMessage deserializeFromBytes(byte[] data, int startPoint) {
        int seqNumber = fromByteArray(data,startPoint);
        int broadcasterID = fromByteArray(data, startPoint+4);
        String payload = new String(Arrays.copyOfRange(data,startPoint+ 8, startPoint+data.length), StandardCharsets.UTF_8);
        return new BroadcastMessage(seqNumber, broadcasterID, payload);
    }
}
