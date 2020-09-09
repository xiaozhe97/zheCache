package com.zhe.zhecache;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HTTPPool {
    private String defaultBasePath = "/zhecache/";
    private int port;
    private String basePath;
    private ZheCache zheCache;

    public HTTPPool(int port, ZheCache zheCache) {
        this.port = port;
        this.zheCache = zheCache;
        basePath = defaultBasePath;
    }

    public void serve() throws IOException {
        ServerSocket ss = new ServerSocket(this.port);
        System.out.println("server is running...");
        while (true) {
            Socket sock = ss.accept();
            System.out.println("connected from " + sock.getRemoteSocketAddress());
            Thread t = new Handler(sock, this.basePath, this.zheCache);
            t.start();
        }
    }
}

class Handler extends Thread {
    Socket sock;
    String basePath;
    ZheCache zheCache;

    public Handler(Socket sock, String basePath, ZheCache zheCache) {
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
            String template = "<html><body>"
                    + key + " : " + data
                    + "</body></html>";
            int length = template.getBytes(StandardCharsets.UTF_8).length;
            writer.write("HTTP/1.0 200 OK\r\n");
            writer.write("Connection: close\r\n");
            writer.write("Content-Type: text/html\r\n");
            writer.write("Content-Length: " + length + "\r\n");
            writer.write("\r\n"); // 空行标识Header和Body的分隔
            writer.write(template);
        }
        writer.flush();
    }
}
