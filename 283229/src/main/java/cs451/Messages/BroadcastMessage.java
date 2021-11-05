package cs451.Messages;

import java.util.Objects;

public class BroadcastMessage extends Message {
    public BroadcastMessage(int srcID, int dstID, int broadcastId, int seqNumber, MessageType mt, String payload, long timeSent) {
        super(srcID, dstID,broadcastId, seqNumber, mt, payload, timeSent);
    }

    public BroadcastMessage(Message m) {
        super(m.srcID, m.dstID, m.broadcasterID, m.seqNumber, m.mt, m.payload);
    }

    public BroadcastMessage(int seqNumber, MessageType mt, String payload) {
        super(seqNumber, mt, payload);
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
