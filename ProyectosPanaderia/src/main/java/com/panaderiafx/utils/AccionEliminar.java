package com.panaderiafx.utils;

import javafx.application.Platform;
import javafx.scene.control.*;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccionEliminar {

    public static void ejecutar(String tabla, Map<String, Control> campos, Label error) {
        error.setText("");
        error.setStyle("-fx-text-fill: red;");
        String codigo = "";

        if (campos.containsKey("codigo") && campos.get("codigo") instanceof TextField tf) {
            codigo = tf.getText().trim();
        }

        if (!ValidacionUtils.esTextoValido(codigo)) {
            error.setText("⚠ 'codigo' no puede estar vacío.");
            return;
        }

        String codigoFinal = codigo;
        AtomicBoolean eliminado = new AtomicBoolean(false);

        Thread eliminarThread = new Thread(() -> {
            boolean ok = ExcelUtils.eliminarPorCodigo(tabla, codigoFinal);
            eliminado.set(true);

            Platform.runLater(() -> {
                if (ok) {
                    error.setStyle("-fx-text-fill: green;");
                    error.setText("✅ Eliminado correctamente.");
                    System.out.println("🗑 Eliminado en " + tabla + ": " + codigoFinal);
                } else {
                    error.setStyle("-fx-text-fill: red;");
                    error.setText("❌ No se pudo eliminar.");
                    TablaViewer.mostrarTabla(tabla);
                }
            });
        });
        eliminarThread.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!eliminado.get()) {
                    Platform.runLater(() -> {
                        error.setStyle("-fx-text-fill: red;");
                        error.setText("❌ La eliminación demoró demasiado.");
                        TablaViewer.mostrarTabla(tabla);
                    });
                }
            }
        }, 3000);
    }
}
