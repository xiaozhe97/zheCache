package com.zhe.zhecache.consistenthash;

public class ConHashMap_test {
    public static void main(String[] args) {
        ConHashMap conHashMap = new ConHashMap(3);
        conHashMap.add("1", "50", "100");
        System.out.println(conHashMap.get("0"));
        System.out.println(conHashMap.get("2"));
        System.out.println(conHashMap.get("8"));
        System.out.println(conHashMap.get("999999"));
    }
}
