package com.zhe.zhecache;

import com.zhe.zhecache.lru.LRUCache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

//封装LRUCache，支持并发读写
public class Cache<K, V> {
    private LRUCache<K, V> lru;
    private final Lock lock = new ReentrantLock();
    private int size;
    private BiConsumer<K, V> callBack;

    public Cache(int size, BiConsumer<K, V> callBack) {
        this.size = size;
        this.callBack = callBack;
    }

    public void put(K k, V v) {
        this.lock.lock();
        if (this.lru == null) {
            this.lru = new LRUCache<>(this.size, callBack);
        }
        this.lru.put(k, v);
        this.lock.unlock();
    }

    public V get(K k) {
        this.lock.lock();
        if (this.lru == null) {
            this.lock.unlock();
            return null;
        }
        V v = this.lru.get(k);
        this.lock.unlock();
        return v;
    }
}
