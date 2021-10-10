package cs451;

import java.nio.ByteBuffer;

public class Test {
    public static void main(String[] args) {
        String s = "89salut ça va";
        byte[] bt = s.getBytes();
        byte [] head = ByteBuffer.allocate(4).putInt(4).array();
        for (byte b  : head )
            System.out.println(b);
    }
}
