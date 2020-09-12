package com.zhe.zhecache.singleflight;

import java.util.concurrent.CountDownLatch;

//代表正在进行中，或已经结束的请求
public class Call {
    private byte[] val;
    private CountDownLatch cld;

    public byte[] getVal() {
        return val;
    }

    public void setVal(byte[] val) {
        this.val = val;
    }

    public void await() {
        try {
            this.cld.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void lock() {
        this.cld = new CountDownLatch(1);
    }

    public void done() {
        this.cld.countDown();
    }
}
