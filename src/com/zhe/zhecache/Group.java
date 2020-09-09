package com.zhe.zhecache;

import java.util.function.Function;

public class Group {
    private final java.lang.String name;
    private final Cache<String, byte[]> mainCache;
    private final Function<String, byte[]> getter;

    public Group(java.lang.String name, Function<String, byte[]> getter, int size) {
        this.name = name;
        this.getter = getter;
        this.mainCache = new Cache<>(size, null);
    }

    public byte[] get(String k) {
        if (k == null) {
            return null;
        }
        byte[] v = this.mainCache.get(k);
        if (v != null) {
            System.out.println("[zheCache] hit");
            return v;
        }
        return this.load(k);
    }

    private byte[] load(String k) {
        return this.getLocally(k);
    }

    private byte[] getLocally(String k) {
        byte[] v = getter.apply(k);
        if (v == null) {
            return null;
        }
        this.populateCache(k, v);
        return v;
    }

    private void populateCache(String k, byte[] v) {
        this.mainCache.put(k, v);
    }
}
