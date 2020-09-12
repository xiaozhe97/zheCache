package com.zhe.zhecache;

import com.zhe.zhecache.lru.Cache;
import com.zhe.zhecache.singleflight.CallManage;

import java.util.function.Function;

public class Group {
    private final java.lang.String name;
    private final Cache<String, byte[]> mainCache;
    private final Function<String, byte[]> getter;
    private HttpPool peers;
    private final CallManage loader;

    public Group(java.lang.String name, Function<String, byte[]> getter, int size) {
        this.name = name;
        this.getter = getter;
        this.mainCache = new Cache<>(size, null);
        this.loader = new CallManage();
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

    public void registerPeers(HttpPool peers) {
        if (this.peers != null) {
            System.out.println("RegisterPeerPicker called more than once");
            return;
        }
        this.peers = peers;
    }

    private byte[] load(String key) {
        return this.loader.run(key, () -> {
            if (this.peers != null) {
                HttpGetter peer = this.peers.pickPeer(key);
                if (peer != null) {
                    byte[] value =  this.getFromPeer(peer, key);
                    if (value != null) {
                        return value;
                    }
                } else {
                    System.out.println("[zheCache] Failed to get from peer cache");
                }
            }
            return this.getLocally(key);
        });
    }

    private byte[] getFromPeer(HttpGetter peer, String key) {
        String res = peer.get(this.name, key);
        if (res == null) {
            return null;
        }
        return ByteArrayUtil.oToB(res);
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
