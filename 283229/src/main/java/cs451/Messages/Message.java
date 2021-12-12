package cs451.Messages;

public abstract class Message {
    protected int seqNumber;

    protected Message(int seqNumber){
        this.seqNumber = seqNumber;
    }

    public int getSeqNumber() {
        return seqNumber;
    }


    protected static int intFromByteArray(byte[] bytes, int  startPoint) {
        return bytes[startPoint] << 24 | (bytes[startPoint+1] & 0xFF) << 16 | (bytes[startPoint+2] & 0xFF) << 8 | (bytes[startPoint+3] & 0xFF);
    }
    protected static long longFromByteArray(byte[] bytes, int  startPoint) {
        int i1 = intFromByteArray(bytes, startPoint);
        int i2 = intFromByteArray(bytes,startPoint+4);
        return ((long)i1 & 0xFFFFFFFF) << 32 | (i2 & 0xFFFFFFFF);
    }
}