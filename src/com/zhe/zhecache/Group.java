package com.zhe.zhecache;

import com.zhe.zhecache.lru.Cache;
import com.zhe.zhecache.singleflight.CallManage;

import java.util.function.Function;

//Group 是 zheCache 最核心的数据结构，负责与用户的交互，并且控制缓存值存储和获取的流程。
//                            是
//接收 key --> 检查是否被缓存 -----> 返回缓存值 (1)
//                |  否                         是
//                |-----> 是否应当从远程节点获取 -----> 与远程节点交互 --> 返回缓存值 (2)
//                            |  否
//                            |-----> 调用`回调函数`，获取值并添加到缓存 --> 返回缓存值 (3)
//
//细化流程（2）：
//使用一致性哈希选择节点        是                                    是
//    |-----> 是否是远程节点 -----> HTTP 客户端访问远程节点 --> 成功？-----> 服务端返回返回值
//                    |  否                                    ↓  否
//                    |----------------------------> 回退到本地节点处理。
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
