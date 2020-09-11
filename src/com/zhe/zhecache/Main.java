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

        String[] peers = new String[]{"localhost:8081", "localhost:8082", "localhost:8083"};
        String groupName = "scores";

        for (String peerKey : peers) {
            ZheCache zheCache = new ZheCache();
            zheCache.newGroup(groupName, 3, key -> {
                System.out.println("[SlowDB] search key " + key);
                Integer value = db.get(key);
                if (value != null) {
                    return ByteArrayUtil.oToB(value);
                }
                return null;
            });
            zheCache.startCacheServer(peerKey, peers, groupName);
        }

        new Thread(() -> {
            ZheCache zheCache = new ZheCache();
            zheCache.newGroup(groupName, 3, key -> {
                System.out.println("[SlowDB] search key " + key);
                Integer value = db.get(key);
                if (value != null) {
                    return ByteArrayUtil.oToB(value);
                }
                return null;
            });
            zheCache.startAPIServer("localhost:8084", peers, groupName);
        }).start();
    }
}
