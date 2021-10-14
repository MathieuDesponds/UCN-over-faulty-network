package cs451;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {
        HashSet<Integer> hs = new HashSet<Integer>();
        List l = new ArrayList<>();
        hs.stream().map(i -> l.get(i)).collect(Collectors.toList());
    }
}
