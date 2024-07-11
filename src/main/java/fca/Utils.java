package fca;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static BitSet toBitSet(Set y, Object[] attributes) {
        BitSet b = new BitSet(attributes.length);
        b.clear();
        for (int i = 0; i < attributes.length; ++i) {
            if (y.contains(attributes[i]))
                b.set(i);
        }
        return(b);
    }

    public static  Set toSet(BitSet y, Object[] attributes) {
        Set s = new HashSet();
        for (int i = y.nextSetBit(0); i >= 0; i = y.nextSetBit(i+1)) {
            s.add(attributes[i]);
        }
        return(s);
    }
}