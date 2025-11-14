package com.myapp.model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App extends Application {
    private static HttpServer server;

    public static void main(String[] args) {
        // Try hardware-accelerated pipeline first (es2). If problems occur,
        // set PRISM_MODE=sw in environment to force software. Also try to
        // disable automatic HiDPI scaling which can cause WebView tile issues.
        String prismMode = System.getenv("PRISM_MODE");
        if (prismMode == null)
            prismMode = "es2"; // default to hardware
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "true");
        System.setProperty("glass.win.uiScale", System.getProperty("glass.win.uiScale", "1.0"));
        System.setProperty("javafx.noSpaceKeyFix", "true");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicia servidor HTTP local
            startLocalServer();

            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Scene + layout
            Scene scene = new Scene(webView, 1024, 768);
            // faz o WebView acompanhar o tamanho do Scene
            webView.prefWidthProperty().bind(scene.widthProperty());
            webView.prefHeightProperty().bind(scene.heightProperty());

            primaryStage.setScene(scene);
            primaryStage.setTitle("OSRM Route Planner");
            primaryStage.setOnCloseRequest(e -> stopServer());
            primaryStage.show();

            // Carrega o mapa com delay maior depois da Stage estar visível
            Platform.runLater(() -> {
                PauseTransition loadDelay = new PauseTransition(Duration.millis(500));
                loadDelay.setOnFinished(ev -> webEngine.load("http://localhost:8888/map.html"));
                loadDelay.play();

                // quando o WebEngine terminar de carregar, force massive invalidations
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        // Immediate invalidations at shorter intervals
                        int[] delays = new int[] { 10, 30, 60, 100, 150, 250, 400, 600, 900, 1200, 1500, 2000 };
                        for (int d : delays) {
                            PauseTransition pt = new PauseTransition(Duration.millis(d));
                            pt.setOnFinished(e2 -> {
                                try {
                                    webEngine.executeScript("if (window.reinitMap) window.reinitMap();");
                                    webEngine.executeScript(
                                            "if (window._mapInstance) window._mapInstance.invalidateSize(true);");
                                } catch (Exception ex) {
                                    // ignore
                                }
                            });
                            pt.play();
                        }

                        // If invalidation didn't help, schedule full recreate attempts
                        int[] recreateDelays = new int[] { 800, 1600, 3000 };
                        for (int d : recreateDelays) {
                            PauseTransition pt2 = new PauseTransition(Duration.millis(d));
                            pt2.setOnFinished(e2 -> {
                                try {
                                    webEngine.executeScript("if (window.recreateMap) window.recreateMap();");
                                } catch (Exception ex) {
                                    // ignore
                                }
                            });
                            pt2.play();
                        }
                    }
                });
            });

            // Resize listeners - call invalidate immediately AND with delay
            scene.widthProperty().addListener((obs, oldV, newV) -> {
                try {
                    webEngine.executeScript("if (window._mapInstance) window._mapInstance.invalidateSize(true);");
                    PauseTransition resizeDelay = new PauseTransition(Duration.millis(100));
                    resizeDelay.setOnFinished(e -> {
                        try {
                            webEngine.executeScript(
                                    "if (window._mapInstance) window._mapInstance.invalidateSize(true);");
                        } catch (Exception ex) {
                            /* ignore */ }
                    });
                    resizeDelay.play();
                } catch (Exception ex) {
                    /* ignore */
                }
                // schedule recreate attempt if invalidation doesn't fix it
                PauseTransition recreateDelay = new PauseTransition(Duration.millis(300));
                recreateDelay.setOnFinished(e -> {
                    try {
                        webEngine.executeScript("if (window.recreateMap) window.recreateMap();");
                    } catch (Exception ex) {
                        /* ignore */ }
                });
                recreateDelay.play();
            });
            scene.heightProperty().addListener((obs, oldV, newV) -> {
                try {
                    webEngine.executeScript("if (window._mapInstance) window._mapInstance.invalidateSize(true);");
                    PauseTransition resizeDelay = new PauseTransition(Duration.millis(100));
                    resizeDelay.setOnFinished(e -> {
                        try {
                            webEngine.executeScript(
                                    "if (window._mapInstance) window._mapInstance.invalidateSize(true);");
                        } catch (Exception ex) {
                            /* ignore */ }
                    });
                    resizeDelay.play();

                    PauseTransition recreateDelay = new PauseTransition(Duration.millis(300));
                    recreateDelay.setOnFinished(e -> {
                        try {
                            webEngine.executeScript("if (window.recreateMap) window.recreateMap();");
                        } catch (Exception ex) {
                            /* ignore */ }
                    });
                    recreateDelay.play();
                } catch (Exception ex) {
                    /* ignore */
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocalServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/") || path.equals("/map.html")) {
                    serveFile(exchange, "/map.html", "text/html");
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor HTTP iniciado em http://localhost:8888");
    }

    private void serveFile(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            byte[] response = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Servidor HTTP parado");
        }
    }
}