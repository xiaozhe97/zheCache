package com.zhe.zhecache.consistenthash;

import com.zhe.zhecache.ByteArrayUtil;

import java.util.*;
import java.util.function.Function;
import java.util.zip.CRC32;

public class ConHashMap {
    //虚拟节点倍数
    private final int replicas;
    //哈希环
    private final List<Long> keys;
    //虚拟节点和真实节点的映射表，键是虚拟节点的哈希值，值是真实节点的名称
    private final Map<Long, String> hashMap;

    private static long hash(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return crc32.getValue();
//        String res = ByteArrayUtil.bToO(data).toString();
//        return Long.parseLong(res);
    }

    public ConHashMap(int replicas) {
        this.replicas = replicas;
        hashMap = new HashMap<>();
        keys = new ArrayList<>();
    }

    public void add(String... keys) {
        for (String key : keys) {
            for (int i = 0; i < this.replicas; i++) {
                byte[] originValue = ByteArrayUtil.oToB(i + key);
                long hashedValue = hash(originValue);
                this.keys.add(hashedValue);
                this.hashMap.put(hashedValue, key);
            }
        }
        Collections.sort(this.keys);
    }

    public String get(String key) {
        if (key.length() == 0) {
            return "";
        }
        long hashedKey = hash(ByteArrayUtil.oToB(key));
        int idx = 0;
        for (int i = 0; i < this.keys.size(); i++) {
            if (this.keys.get(i) >= hashedKey) {
                idx = i;
                break;
            }
        }
        long peerKey = this.keys.get(idx % this.keys.size());
        return this.hashMap.get(peerKey);
    }
}
