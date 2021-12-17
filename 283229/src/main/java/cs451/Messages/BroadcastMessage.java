package cs451.Messages;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class BroadcastMessage extends Message {
    protected int broadcasterID;
    protected String payload;
    protected int[] vc;
    protected int byteSize;
    protected static int[][] causality;
    protected static int nbHosts;

    public BroadcastMessage(int seqNumber, int broadcasterID, String payload, int [] vc) {
        super(seqNumber);
        this.broadcasterID = broadcasterID;
        this.payload = payload;
        if(vc != null) {
            this.vc = Arrays.copyOf(vc, vc.length);
            this.byteSize = 12 + payload.getBytes().length + causality[broadcasterID-1][nbHosts] * 4;
        }
    }

    public BroadcastMessage(int seqNumber, String payload) { //used in the main
        this(seqNumber, -1, payload, null);

    }

    public static void setCausalityNBHosts(int[][] c, int nbH) {
        nbHosts = nbH;
        causality = c;
    }

    public int getBroadcasterID() {
        return broadcasterID;
    }

    public void setBroadcasterID(int broadcasterID) {
        this.broadcasterID = broadcasterID;
    }

    public String getPayload() {
        return payload;
    }

    public int[] getVC() {
        return vc;
    }

    public void setVC(int[] vc) {
        this.vc = Arrays.copyOf(vc,vc.length);
    }

    public int getByteSize() {
        return byteSize;
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
        String s =  "BM {" +
                "brdID=" + broadcasterID +
                ", seqNumber=" + seqNumber +
                ", payload=" + payload+
                ", byteSize=" + byteSize+
                ", vc= [";
        for(int i = 0 ; i<vc.length; i++){
            s+= vc[i]+", ";
        }
        s+="] }";
        return s;
    }


    public static BroadcastMessage deserializeFromBytes(byte[] data, int startPoint) {
        int current = startPoint;
        int seqNumber = intFromByteArray(data,current); current +=4;
        int broadcasterID = intFromByteArray(data, current);current +=4;
        int payloadSize = intFromByteArray(data, current);current +=4;
        String payload;
        if(payloadSize == 0)
            payload = "";
        else {
            payload = new String(Arrays.copyOfRange(data, current, current + payloadSize), StandardCharsets.UTF_8);
            current += payloadSize;
        }
        int[] vc = new int[nbHosts];
        for(int i = 0 ; i<vc.length; i++){
            if(causality[broadcasterID-1][i] == 1) {
                vc[i] = intFromByteArray(data, current);
                current += 4;
            }else{
                vc[i] = -1;
            }
        }
        return new BroadcastMessage(seqNumber, broadcasterID, payload, vc);
    }
}
