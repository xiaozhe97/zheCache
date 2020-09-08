package com.zhe.zhecache;

import java.util.function.Function;

public class Group<K, V> {
    private final String name;
    private final Cache<K, V> mainCache;
    private final Function<K, V> getter;

    public Group(String name, Function<K, V> getter, int size) {
        this.name = name;
        this.getter = getter;
        this.mainCache = new Cache<>(size, null);
    }

    public V get(K k) {
        if (k == null) {
            return null;
        }
        V v = this.mainCache.get(k);
        if (v != null) {
            System.out.println("[zheCache] hit");
            return v;
        }
        return this.load(k);
    }

    private V load(K k) {
        return this.getLocally(k);
    }

    private V getLocally(K k) {
        V v = getter.apply(k);
        if (v == null) {
            return null;
        }
        this.populateCache(k, v);
        return v;
    }

    private void populateCache(K k, V v) {
        this.mainCache.put(k, v);
    }
}
