package com.zhe.zhecache;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ZheCache {
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock lock = new ReentrantLock();
    private final Map<String, Group> groups = new HashMap<>();

    public Group newGroup(String name, int size, Function<String, byte[]> getter) {
        if (getter == null) {
            throw new IllegalArgumentException("null getter");
        }
        lock.lock();
        Group g = new Group(name, getter, size);
        groups.put(name, g);
        lock.unlock();
        return g;
    }

    public Group getGroup(String name) {
        rwlock.readLock().lock();
        Group g = groups.get(name);
        rwlock.readLock().unlock();
        return g;
    }

    public void serve(String ip, int port) {
        HttpPool httpPool = new HttpPool(port, ip,this);
        new Thread(() -> {
            try {
                httpPool.serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startCacheServer(String peerKey ,String[] peersAddr, String groupName) {
        Group g = this.getGroup(groupName);
        String[] tmp = peerKey.split(":");
        String ip = tmp[0];
        int port = Integer.parseInt(tmp[1]);
        HttpPool peers = new HttpPool(port, ip, this);
        peers.set(peersAddr);
        g.registerPeers(peers);
        System.out.println("zheCache is running at " + peerKey);
        new Thread(() -> {
            try {
                peers.serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startAPIServer(String apiAddr, String[] peersAddr, String groupName) {
        Group g = this.getGroup(groupName);
        String[] tmp = apiAddr.split(":");
        String ip = tmp[0];
        int port = Integer.parseInt(tmp[1]);
        HttpPool peers = new HttpPool(port, ip, this);
        peers.set(peersAddr);
        g.registerPeers(peers);
        HttpServer httpServer = null;
        System.out.println("api server start at " + apiAddr);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getByName(ip), port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpServer.createContext("/api", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                URI uri = httpExchange.getRequestURI();
                String path = uri.getPath();
                String key = path.split("/")[2];
                byte[] respContents = ByteArrayUtil.bToO(g.get(key)).toString().getBytes("UTF-8");
                httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                httpExchange.sendResponseHeaders(200, respContents.length);
                httpExchange.getResponseBody().write(respContents);
                httpExchange.close();
            }
        });
        httpServer.start();
    }

}
