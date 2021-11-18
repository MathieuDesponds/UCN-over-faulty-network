package cs451.Messages;

import java.util.Objects;

public class BroadcastMessage extends Message {
    private int broadcasterID;
    private String payload;
    private int dstId;

    public BroadcastMessage(int seqNumber, String payload) {
        super(seqNumber);
        this.broadcasterID = -1;
        this.payload = payload;
    }

    public BroadcastMessage(BroadcastMessage bm, int dstId) {
        super(bm.getSeqNumber());
        this.broadcasterID = bm.getBroadcasterID();
        this.dstId = dstId;
    }

    public String getPayload() {
        return payload;
    }

    public int getDstId() {
        return dstId;
    }

    public void setDstId(int dstId) {
        this.dstId = dstId;
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
}
