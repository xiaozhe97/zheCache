package com.zhe.zhecache;

import com.zhe.zhecache.lru.LRUCache;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, Integer> db = new HashMap<>();
        db.put("Tom", 123);
        db.put("Jack", 456);
        db.put("Sam", 789);
        ZheCache<String, Integer> z = new ZheCache<>();
        Group<String, Integer> g = z.NewGroup("scores", 3, key -> {
            System.out.println("[SlowDB] search key " + key);
            Integer value = db.get(key);
            if (value != null) {
                return value;
            }
            return null;
        });
        System.out.println(g.get("Tom"));
        System.out.println(g.get("Tom"));
    }
}
