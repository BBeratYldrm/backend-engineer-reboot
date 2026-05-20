package com.berat.reboot.javacore;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Time Complexity:  O(1) — get and put both O(1)
 * Space Complexity: O(capacity) — cache holds at most capacity elements
 *
 * LRU Cache — evicts the least recently used element when capacity is exceeded.
 * LinkedHashMap with accessOrder=true moves accessed elements to the tail.
 * Head = oldest (least recently used), Tail = newest (most recently used).
 * removeEldestEntry automatically evicts when size exceeds capacity.
 */

public class LRUCache {

    private final int capacity;
    private final LinkedHashMap<Integer, Integer> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        return cache.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        cache.put(key, value);
    }

    public static void main(String[] args) {
        LRUCache lru = new LRUCache(2);

        lru.put(1, 1);
        lru.put(2, 2);
        System.out.println(lru.get(1));  // 1
        lru.put(3, 3);                   // 2 atılır
        System.out.println(lru.get(2));  // -1
        lru.put(4, 4);                   // 1 atılır
        System.out.println(lru.get(1));  // -1
        System.out.println(lru.get(3));  // 3
        System.out.println(lru.get(4));  // 4
    }
}