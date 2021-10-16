package cs451;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        byte [] b = {0,0,0,1,0,0,0,2};
        System.out.print(ByteBuffer.wrap(Arrays.copyOfRange(b,0,8)).getInt(2));

    }
}
