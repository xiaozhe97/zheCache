package com.zhe.zhecache.lru;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class LRUCache<K, V> {
    private int size;
    private HashMap<K, Node> map;
    private Node head;
    private Node tail;
    //删除后的回调函数
    private BiConsumer<K, V> callBack;

    class Node {
        K k;
        V v;
        Node pre;
        Node next;

        Node(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }

    public LRUCache(int size, BiConsumer<K, V> func) {
        this.size = size;
        map = new HashMap<>();
        callBack = func;
    }

    /**
     * 添加/修改元素
     * 1.元素存在，将元素移动到队尾
     * 2.不存在，判断链表是否满。
     * 如果满，则删除队首元素，放入队尾元素，删除更新哈希表
     * 如果不满，放入队尾元素，更新哈希表
     */
    public void put(K key, V value) {
        Node node = map.get(key);
        if (node != null) {
            //更新值
            node.v = value;
            moveNodeToTail(node);
        } else {
            Node newNode = new Node(key, value);
            //链表满，需要删除首节点
            if (map.size() == size) {
                Node delHead = removeHead();
                map.remove(delHead.k);
                if (callBack != null) {
                    callBack.accept(delHead.k, delHead.v);
                }
            }
            addLast(newNode);
            map.put(key, newNode);
        }
    }

    public V get(K key) {
        Node node = map.get(key);
        if (node != null) {
            moveNodeToTail(node);
            return node.v;
        }
        return null;
    }

    public void addLast(Node newNode) {
        if (newNode == null) {
            return;
        }
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            //连接新节点
            tail.next = newNode;
            newNode.pre = tail;
            //更新尾节点指针为新节点
            tail = newNode;
        }
    }

    public void moveNodeToTail(Node node) {
        if (tail == node) {
            return;
        }
        if (head == node) {
            head = node.next;
            head.pre = null;
        } else {
            //调整双向链表指针
            node.pre.next = node.next;
            node.next.pre = node.pre;
        }
        node.pre = tail;
        node.next = null;
        tail.next = node;
        tail = node;
    }

    public Node removeHead() {
        if (head == null) {
            return null;
        }
        Node res = head;
        if (head == tail) {
            head = null;
            tail = null;
        } else {
            head = res.next;
            head.pre = null;
            res.next = null;
        }
        return res;
    }
}