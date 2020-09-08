package com.zhe.zhecache;

import com.zhe.zhecache.lru.LRUCache;

public class Main {

    public static void main(String[] args) {
	    String k1 = "tom";
	    int v1 = 123;
        String k2 = "jack";
        int v2 = 456;
        String k3 = "lee";
        int v3 = 789;
        Cache<String, Integer> lru = new Cache<>(3, null);
//        LRUCache<String, Integer> lru = new LRUCache<>(3, null);
        lru.put(k1, v1);
        lru.put(k2, v2);
        lru.put(k3, v3);
        new Thread(() -> {
            System.out.println(lru.get(k1));
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(lru.get(k1));
        }).start();
        new Thread(() -> {
            lru.put(k1, 111);
        }).start();
    }
}
