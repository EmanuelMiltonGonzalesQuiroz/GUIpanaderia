package com.panaderiafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setCenter(Pestanas.crear());

        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().setStyle("-fx-font-size: 14px; -fx-background-color: #FFF8E1;");

        primaryStage.setTitle("Sistema de Panadería ");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        // Cargar ícono desde carpeta icons
        File iconFile = new File("icons/icon.png"); // O usa .ico si estás seguro
        if (iconFile.exists()) {
            Image icon = new Image(iconFile.toURI().toString());
            primaryStage.getIcons().add(icon);
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
