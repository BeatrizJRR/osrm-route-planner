package com.myapp.model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Carrega o ficheiro HTML do mapa
        URL url = getClass().getResource("/com/myapp/resources/map.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            System.out.println("map.html não encontrado!");
        }

        Scene scene = new Scene(webView, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("OSRM Route Planner");
        primaryStage.show();
    }
}

