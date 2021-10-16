package cs451;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        List<Integer> l = List.of(1,2,3,4,5,6,7,8);
        for(int i : l.subList(0,0))
            System.out.print(i);
    }
}
