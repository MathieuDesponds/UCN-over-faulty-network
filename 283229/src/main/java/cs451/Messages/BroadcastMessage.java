package cs451.Messages;

import java.util.Objects;

public class BroadcastMessage extends Message {
    private String payload;
    private int dstId;

    public BroadcastMessage(int broadcastId, int seqNumber, String payload) {
        super(broadcastId, seqNumber);
        this.payload = payload;
    }

    public BroadcastMessage(int seqNumber, String payload) {
        super(-1, seqNumber);
        this.payload = payload;
    }

    public void setDstId(int dstId) {
        this.dstId = dstId;
    }

    public int getDstId() {
        return dstId;
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
}
