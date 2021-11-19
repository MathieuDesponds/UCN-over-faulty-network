package cs451;

import cs451.Messages.BroadcastMessage;
import cs451.Messages.Packet;

public class Tests {
    public static void main(String[] args) {
        BroadcastMessage bm1 = new BroadcastMessage(1,2,"");
        BroadcastMessage bm2 = new BroadcastMessage(2,3,"");

        Packet p = new Packet(1,1 , Packet.MessageType.MESSAGE,-1);
        p.addBM(bm1); p.addBM(bm2);

        byte[] pb = p.serializeToBytes();

        System.out.println(Packet.deserializeFromBytes(pb));
    }
}