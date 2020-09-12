package com.zhe.zhecache.singleflight;

import com.zhe.zhecache.ByteArrayUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

//singleflight 的主数据结构，管理不同 key 的请求(call)
public class CallManage {
    private final Lock lock = new ReentrantLock();
    private Map<String, Call> callMap;

    public byte[] run(String key, Supplier<byte[]> func) {
        this.lock.lock();
        if (this.callMap == null) {
            this.callMap = new HashMap<>();
        }
        Call call = this.callMap.get(key);
        if (call != null) {
            this.lock.unlock();
            call.await();
            return call.getVal();
        }
        call = new Call();
        call.lock();
        this.callMap.put(key, call);
        this.lock.unlock();

        call.setVal(func.get());
        call.done();

        this.lock.lock();
        this.callMap.remove(key);
        this.lock.unlock();

        return call.getVal();
    }

    //测试singleflight，无论同时进行多少次请求，func都只执行一次
    public static void main(String[] args) {
        CallManage callManage = new CallManage();
        int count = 10;
        CountDownLatch cld = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                try {
                    cld.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] value = callManage.run("key", () -> {
                    System.out.println("func");
                    return ByteArrayUtil.oToB("bar");
                });
                System.out.println(ByteArrayUtil.bToO(value).toString());
            }).start();
            cld.countDown();
        }
    }
}
