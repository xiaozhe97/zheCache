package com.zhe.zhecache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ZheCache {
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock lock = new ReentrantLock();
    private final Map<String, Group> groups = new HashMap<>();

    public Group newGroup(String name, int size, Function<String, byte[]> getter) {
        if (getter == null) {
            throw new IllegalArgumentException("null getter");
        }
        lock.lock();
        Group g = new Group(name, getter, size);
        groups.put(name, g);
        lock.unlock();
        return g;
    }

    public Group getGroup(String name) {
        rwlock.readLock().lock();
        Group g = groups.get(name);
        rwlock.readLock().unlock();
        return g;
    }

    public void serve(int port) {
        new Thread(() -> {
            try {
                new HTTPPool(port, this).serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
