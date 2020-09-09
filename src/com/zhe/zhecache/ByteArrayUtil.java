package com.zhe.zhecache;

import java.io.*;

//字节数组工具类
public class ByteArrayUtil {
    //对象转字节数组
    public static <T> byte[] oToB(T obj){
        byte[] bytes = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream sOut;
        try {
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(obj);
            sOut.flush();
            bytes= out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    //字节数组转对象
    public static Object bToO(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn;
        try {
            sIn = new ObjectInputStream(in);
            obj = sIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
