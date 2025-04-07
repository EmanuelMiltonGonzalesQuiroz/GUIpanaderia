package com.panaderiafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setCenter(Pestanas.crear());

        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().setStyle("-fx-font-size: 14px; -fx-background-color: #FFF8E1;"); // fondo panadería

        primaryStage.setTitle("Sistema de Panadería 🍞");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
