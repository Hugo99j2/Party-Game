package com.hugo99j.chaosparty.util;

import java.util.ArrayList;
import java.util.List;

public class Looper {
    public static  <T> T nextValue(List<T> list, T current) {
        int index = list.indexOf(current);
        if(index+1 >= list.size()) return list.getFirst();
        return list.get(index+1);
    }

    public static  <T extends Enum<T>> T nextValue(T current) {
        int index = current.ordinal();
        if(index+1 >= current.getDeclaringClass().getEnumConstants().length) return current.getDeclaringClass().getEnumConstants()[0];
        return current.getDeclaringClass().getEnumConstants()[index+1];
    }

    public static  <T> T previousValue(List<T> list, T current) {
        int index = list.indexOf(current);
        if(index-1 < 0) return list.getLast();
        return list.get(index-1);
    }

    public static  <T extends Enum<T>> T previousValue(T current) {
        int index = current.ordinal();
        if(index-1 < 0) return current.getDeclaringClass().getEnumConstants()[current.getDeclaringClass().getEnumConstants().length-1];
        return current.getDeclaringClass().getEnumConstants()[index-1];
    }
}
