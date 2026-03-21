package com.berat.reboot.javacore;

import java.util.*;

public class CollectionsDemo {

    public static void main(String[] args) {

        // ArrayList
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        System.out.println("ArrayList: " + list);

        // LinkedList
        List<Integer> linked = new LinkedList<>();
        linked.add(10);
        linked.add(20);

        System.out.println("LinkedList: " + linked);

        // HashMap
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);

        System.out.println("HashMap: " + map);

        // HashSet
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(1);
        set.add(2);

        System.out.println("HashSet: " + set);

    }
}