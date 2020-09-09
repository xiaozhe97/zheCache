package com.zhe.zhecache;

import com.zhe.zhecache.lru.LRUCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Integer> db = new HashMap<>();
        db.put("Tom", 123);
        db.put("Jack", 456);
        db.put("Sam", 789);

        ZheCache zheCache = new ZheCache();
        zheCache.serve(8080);
        zheCache.newGroup("scores", 3, key -> {
            System.out.println("[SlowDB] search key " + key);
            Integer value = db.get(key);
            if (value != null) {
                return ByteArrayUtil.oToB(value);
            }
            return null;
        });
    }
}
