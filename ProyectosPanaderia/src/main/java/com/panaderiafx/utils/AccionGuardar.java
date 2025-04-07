package com.panaderiafx.utils;

import javafx.application.Platform;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccionGuardar {

    public static void ejecutar(String tabla, Map<String, Control> campos, Label error) {
        error.setText("");
        error.setStyle("-fx-text-fill: red;"); // Reset estilo
        Map<String, String> datos = new LinkedHashMap<>();

        for (var e : campos.entrySet()) {
            String k = e.getKey();
            Control c = e.getValue();
            String v = "";

            if (c instanceof TextField tf) v = tf.getText().trim();
            else if (c instanceof ComboBox<?> cb && cb.getValue() != null) v = cb.getValue().toString();

            if (!k.equals("precio") && !k.startsWith("_") && !ValidacionUtils.esTextoValido(v)) {
                error.setText("⚠ Campo '" + k + "' es obligatorio.");
                return;
            }

            datos.put(k, v);
        }

        if (!ValidacionUtils.esNumeroValido(datos.get("precio"))) {
            error.setText("⚠ Ingrese un precio válido.");
            return;
        }

        double precio = Double.parseDouble(datos.get("precio"));
        double tasa = TasaCambioUtils.obtenerUltima();
        boolean esLocal = ((RadioButton) campos.get("_radio_local")).isSelected();
        double precioLocal = esLocal ? precio : precio * tasa;
        double precioDolar = esLocal ? precio / tasa : precio;

        datos.put("Código", CodigoGenerator.generar("Ingredientes"));
        datos.put("Nombre", campos.get("nombre") instanceof TextField tf ? tf.getText().trim() : "");
        datos.put("Unidad", campos.get("unidad") instanceof ComboBox<?> cb && cb.getValue() != null ? cb.getValue().toString() : "");
        datos.put("Precio Local", String.format("%.4f", precioLocal));
        datos.put("Precio Dólar", String.format("%.4f", precioDolar));
        datos.put("Fecha de actualización", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        AtomicBoolean guardado = new AtomicBoolean(false);

        Thread guardarThread = new Thread(() -> {
            boolean ok = ExcelUtils.guardarEnTabla(tabla, datos);
            guardado.set(true);

            Platform.runLater(() -> {
                if (ok) {
                    error.setStyle("-fx-text-fill: green;");
                    error.setText("✅ Guardado correctamente.");
                    System.out.println("✅ Guardado en " + tabla + ": " + datos);
                } else {
                    error.setStyle("-fx-text-fill: red;");
                    error.setText("❌ No se pudo guardar en la tabla.");
                    TablaViewer.mostrarTabla(tabla);
                }
            });
        });
        guardarThread.start();

        // Timer para controlar si demora más de 3 segundos
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!guardado.get()) {
                    Platform.runLater(() -> {
                        error.setStyle("-fx-text-fill: red;");
                        error.setText("❌ Guardado demoró demasiado. Verifica conexión.");
                        TablaViewer.mostrarTabla(tabla);
                    });
                }
            }
        }, 3000);
    }
}
