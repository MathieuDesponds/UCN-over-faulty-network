package cs451.Messages;

import java.nio.ByteBuffer;

public class BroadcastMessageSent extends BroadcastMessage{
    private int dstId; //only when sent
    private byte [] bytes;//only when sent
    private int byteSize; //Only when sent

    public BroadcastMessageSent(BroadcastMessage bm, int dstId) {
        super(bm.getSeqNumber(), bm.getBroadcasterID(), bm.getPayload(), bm.getVC());
        this.dstId = dstId;
        bytes = serializeToBytes();
        byteSize = bytes.length;
    }

    public int getDstId() {
        return dstId;
    }

    public int getByteSize(){
        return byteSize;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public byte[] serializeToBytes() {
        byte[] payloadByte = payload.getBytes();
        int size = 12+payloadByte.length + vc.length*4;

        ByteBuffer bb = ByteBuffer.allocate(12+payloadByte.length).putInt(seqNumber).putInt(broadcasterID).putInt(payloadByte.length);
        if(payloadByte.length != 0)
            bb.put(payloadByte);
        bb.put((byte)vc.length);
        for(int i = 0; i<vc.length; i++){
            bb.putInt(vc[i]);
        }
        return bb.array();
    }

}
