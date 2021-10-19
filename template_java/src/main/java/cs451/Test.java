package cs451;

import java.util.ArrayDeque;

public class Test {
    public static void main(String[] args) {
        ArrayDeque<Integer> a = new ArrayDeque<>();
        a.add(1);a.add(2);
        int i = a.getFirst();
        System.out.print(a.size());

    }
}
