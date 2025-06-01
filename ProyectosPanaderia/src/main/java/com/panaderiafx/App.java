package com.panaderiafx;

import com.panaderiafx.utils.VerUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class App extends Application {

    private static long startGlobal;

    @Override
    public void start(Stage primaryStage) {
        long startStart = System.nanoTime();
        System.out.println("✅ Entrando a start()...");

        // 🔄 Cargar todo el Excel en memoria antes de iniciar
        VerUtils.cargarTodoElExcel();

        BorderPane root = new BorderPane();

        long startPestanas = System.nanoTime();
        root.setCenter(Pestanas.crear());
        long endPestanas = System.nanoTime();
        System.out.println("⏱️ Tiempo en crear pestañas: " + ((endPestanas - startPestanas) / 1_000_000) + " ms");

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

        primaryStage.show();

        long endStart = System.nanoTime();
        System.out.println("✅ Mostrando ventana principal...");
        System.out.println("⏱️ Tiempo total en start(): " + ((endStart - startStart) / 1_000_000) + " ms");
        System.out.println("⏱️ Tiempo desde main() hasta GUI: " + ((endStart - startGlobal) / 1_000_000) + " ms");
    }

    public static void main(String[] args) {
        startGlobal = System.nanoTime();  // ⏱️ Inicio global

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
