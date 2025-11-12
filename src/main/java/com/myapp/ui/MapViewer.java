package com.myapp.ui;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.layout.BorderPane;

public class MapViewer extends BorderPane {
    private WebView webView;
    private WebEngine webEngine;

    public MapViewer() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Carrega o mapa a partir do ficheiro HTML
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        this.setCenter(webView);
    }
}
