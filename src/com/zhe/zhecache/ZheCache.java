package com.zhe.zhecache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ZheCache<K, V> {
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock lock = new ReentrantLock();
    private final Map<String, Group<K, V>> groups = new HashMap<>();

    public Group<K, V> NewGroup(String name, int size, Function<K, V> getter) {
        if (getter == null) {
            throw new IllegalArgumentException("null getter");
        }
        lock.lock();
        Group<K, V> g = new Group<K, V>(name, getter, size);
        groups.put(name, g);
        lock.unlock();
        return g;
    }

    public Group<K, V> GetGroup(String name) {
        rwlock.readLock().lock();
        Group<K, V> g = groups.get(name);
        rwlock.readLock().unlock();
        return g;
    }
}
