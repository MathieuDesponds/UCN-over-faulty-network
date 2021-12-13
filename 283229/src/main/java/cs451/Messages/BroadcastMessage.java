package cs451.Messages;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class BroadcastMessage extends Message {
    private int broadcasterID;
    private String payload;
    private transient int dstId;
    private byte [] bytes;
    private int byteSize;
    private int[] vc;
    private long sumVC;

    public BroadcastMessage(int seqNumber, int broadcasterID, String payload, int byteSize) {
        super(seqNumber);
        this.broadcasterID = broadcasterID;
        this.payload = payload;
        this.bytes = null;
        this.byteSize = byteSize;
    }
    public BroadcastMessage(int seqNumber, int broadcasterID, String payload) {
        super(seqNumber);
        this.broadcasterID = broadcasterID;
        this.payload = payload;
        bytes = serializeToBytes();
        byteSize = bytes.length;
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

    public int getByteSize(){
        return byteSize;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void addVC(int[] vc) {
        this.vc = Arrays.copyOf(vc,vc.length);
        for(int i= 0; i<vc.length; i++){
            sumVC += vc[i];
        }
    }

    public int[] getVC() {
        return vc;
    }

    public long getSumVC() {
        return sumVC;
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
                ", payload=" + payload +
                '}';
    }
    public byte[] serializeToBytes() {
        byte[] payloadByte = payload.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(12+payloadByte.length).putInt(seqNumber).putInt(broadcasterID).putInt(payloadByte.length);
        if(payloadByte.length != 0)
            bb.put(payloadByte);
        return bb.array();
    }

    public static BroadcastMessage deserializeFromBytes(byte[] data, int startPoint) {
        int seqNumber = intFromByteArray(data,startPoint);
        int broadcasterID = intFromByteArray(data, startPoint+4);
        int payloadSize = intFromByteArray(data, startPoint+8);
        String payload;
        if(payloadSize == 0)
            payload = "";
        else
            payload =  new String(Arrays.copyOfRange(data,startPoint+ 12, startPoint+12+payloadSize), StandardCharsets.UTF_8);
        return new BroadcastMessage(seqNumber, broadcasterID, payload, 12+payloadSize);
    }
}
