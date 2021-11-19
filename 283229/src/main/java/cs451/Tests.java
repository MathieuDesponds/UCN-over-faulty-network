package cs451;

import cs451.Messages.BroadcastMessage;

public class Tests {
    public static void main(String[] args) {
        byte [] l = new BroadcastMessage(1,2,"alksl").serializeToBytes();
        BroadcastMessage bm = BroadcastMessage.deserializeFromBytes(l,0);
        System.out.println(bm);
    }
}