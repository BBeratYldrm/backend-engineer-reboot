package com.berat.reboot.javacore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCacheThreadSafe {

    private final int capacity;
    // accessOrder=true: get() erişimi entry'yi tail'e taşır
    // Head = least recently used, Tail = most recently used
    private final LinkedHashMap<Integer, Integer> cache;
    // ReadWriteLock: normalde read-heavy cache'ler için ideal
    // Ama accessOrder=true nedeniyle get() de write sayılır
    // Bu yüzden her iki metod da writeLock kullanır
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCacheThreadSafe(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                // size > capacity olunca head'deki entry otomatik silinir
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        // writeLock: accessOrder=true nedeniyle get() sırayı değiştirir
        // readLock kullansak race condition oluşur
        lock.writeLock().lock();
        try {
            return cache.getOrDefault(key, -1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(int key, int value) {
        // writeLock: map'e yeni entry ekleniyor veya güncelleniyor
        // removeEldestEntry otomatik çalışır, ekstra kod yok
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}