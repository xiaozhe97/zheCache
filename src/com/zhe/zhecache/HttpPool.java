package com.zhe.zhecache;

import com.zhe.zhecache.consistenthash.ConHashMap;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HttpPool {
    private final Lock lock = new ReentrantLock();
    private String defaultBasePath = "/zhecache/";
    private int port;
    private String basePath;
    private ZheCache zheCache;
    private String ip;
    private ConHashMap peers;
    private Map<String, HttpGetter> httpGetters;

    public HttpPool(int port, String ip, ZheCache zheCache) {
        this.port = port;
        this.zheCache = zheCache;
        basePath = defaultBasePath;
        this.ip = ip;
        this.httpGetters = new HashMap<>();
    }

    public void set(String... peers) {
        lock.lock();
        this.peers = new ConHashMap(3);
        this.peers.add(peers);
        for (String peer : peers) {
            this.httpGetters.put(peer, new HttpGetter(peer));
        }
        lock.unlock();
    }

    public HttpGetter pickPeer(String key) {
        lock.lock();
        String peer = this.peers.get(key);
        if (peer != null && !peer.equals(this.ip + ":" + this.port)) {
            System.out.println("Pick peer " + peer);
            lock.unlock();
            return this.httpGetters.get(peer);
        }
        lock.unlock();
        return null;
    }

    public void serve() throws IOException {
        ServerSocket ss = new ServerSocket(this.port, 50, InetAddress.getByName(this.ip));
        while (true) {
            Socket sock = ss.accept();
            System.out.println("[" + this.ip + ":" + this.port + "]");
            System.out.println("connected from " + sock.getRemoteSocketAddress());
            Thread t = new HttpHandler(sock, this.basePath, this.zheCache);
            t.start();
        }
    }
}

class HttpHandler extends Thread {
    Socket sock;
    String basePath;
    ZheCache zheCache;

    public HttpHandler(Socket sock, String basePath, ZheCache zheCache) {
        this.sock = sock;
        this.basePath = basePath;
        this.zheCache = zheCache;
    }

    @Override
    public void run() {
        try (InputStream input = this.sock.getInputStream()) {
            try (OutputStream output = this.sock.getOutputStream()) {
                HttpHandle(input, output);
            }
        } catch (Exception e) {
            try {
                this.sock.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("client disconnected.");
        }
    }

    private void HttpHandle(InputStream input, OutputStream output) throws IOException {
        System.out.println("Process new http request...");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        boolean requestOK = false;
        String first = reader.readLine();
        if (first.contains("HTTP/1.") && first.contains(this.basePath)) {
            requestOK = true;
        }
        String[] path = first.split(" ")[1].split("/");
        String groupName = path[2];
        String key = path[3];
        System.out.println(requestOK? "Response OK" : "Response Error");
        Group group = zheCache.getGroup(groupName);
        byte[] value = null;
        if (group == null) {
            requestOK = false;
        } else {
            value = group.get(key);
            if (value == null) {
                requestOK = false;
            }
        }
        if (!requestOK) {
            // 发送错误响应:
            writer.write("404 Not Found\r\n");
            writer.write("Content-Length: 0\r\n");
            writer.write("\r\n");
        } else {
            // 发送成功响应:
            String data = ByteArrayUtil.bToO(value).toString();
            int length = data.getBytes(StandardCharsets.UTF_8).length;
            writer.write("HTTP/1.0 200 OK\r\n");
            writer.write("Connection: close\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + length + "\r\n");
            writer.write("\r\n"); // 空行标识Header和Body的分隔
            writer.write(data);
        }
        writer.flush();
    }
}
