package com.myapp.ui;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.BorderPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MapViewer extends BorderPane {
    private WebView webView;
    private WebEngine webEngine;

    public MapViewer() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Carrega o mapa a partir do ficheiro HTML como string
        String htmlContent = loadHtmlContent();
        webEngine.loadContent(htmlContent, "text/html");

        this.setCenter(webView);
    }

    private String loadHtmlContent() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/map.html"), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body>Erro ao carregar o mapa</body></html>";
        }
    }
}
