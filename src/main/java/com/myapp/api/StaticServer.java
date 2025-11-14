package com.myapp.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class StaticServer {
    public static void main(String[] args) throws Exception {
        int port = 8888;
        String base = "src/main/resources";
        if (args.length > 0)
            base = args[0];
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Starting static server on http://localhost:" + port + " serving " + base);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/"))
                    path = "/map.html";
                File f = new File(base + path);
                if (!f.exists() || f.isDirectory()) {
                    String notFound = "404";
                    exchange.sendResponseHeaders(404, notFound.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFound.getBytes());
                    }
                    return;
                }
                String contentType = Files.probeContentType(f.toPath());
                if (contentType == null)
                    contentType = "application/octet-stream";
                byte[] bytes = Files.readAllBytes(f.toPath());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });
        server.setExecutor(null);
        server.start();
        // keep running
        Thread.currentThread().join();
    }
}
