package com.panaderiafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("✅ Entrando a start()...");

        BorderPane root = new BorderPane();
        root.setCenter(Pestanas.crear());

        Scene scene = new Scene(root, 1000, 700);
        scene.getRoot().setStyle("-fx-font-size: 14px; -fx-background-color:rgb(231, 134, 43);");

        primaryStage.setTitle("Sistema de Panadería");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        File iconFile = new File("icons/icon.png");
        if (iconFile.exists()) {
            Image icon = new Image(iconFile.toURI().toString());
            primaryStage.getIcons().add(icon);
        } else {
            System.out.println("⚠️ Icono no encontrado en: " + iconFile.getAbsolutePath());
        }

        System.out.println("✅ Mostrando ventana principal...");
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Redirigir logs desde el principio
        try {
            FileOutputStream fos = new FileOutputStream("log.txt", false); // limpia en cada ejecución
            PrintStream log = new PrintStream(fos, true, "UTF-8");
            System.setOut(log);
            System.setErr(log);
            System.out.println("📌 Redirección de logs activada.");
        } catch (Exception e) {
            System.err.println("❌ No se pudo redirigir salida a log.txt");
            e.printStackTrace();
        }

        System.out.println("🟢 Lanzando aplicación desde main()");
        launch(args);
    }
}
