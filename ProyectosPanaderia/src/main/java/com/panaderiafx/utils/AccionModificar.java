package com.panaderiafx.utils;

import com.panaderiafx.config.PrecioModificableBuilder;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccionModificar {

    public static void ejecutar(String tabla, Map<String, Node> campos, Label error) {
        error.setText("");
        error.setStyle("-fx-text-fill: red;");
        Map<String, String> datos = new LinkedHashMap<>();

        String codigo = obtenerTexto(campos.get("codigo"));
        if (!ValidacionUtils.esTextoValido(codigo)) {
            error.setText("⚠ 'codigo' no puede estar vacío.");
            return;
        }

        String nombre = obtenerTexto(campos.get("nombre"));
        String unidad = obtenerTexto(campos.get("unidad"));
        String categoria = obtenerTexto(campos.get("categoria"));

        // ⚠️ Obtener el VBox que contiene el campo "precio"
        Node nodo = campos.get("precio");
        if (!(nodo instanceof VBox vb)) {
            error.setText("❌ No se puede acceder al campo de precio.");
            return;
        }

        Object dato = vb.getUserData();
        if (!(dato instanceof PrecioModificableBuilder.PrecioData data)) {
            error.setText("❌ No se puede acceder al campo de precio.");
            return;
        }

        String textoPrecio = data.campo.getText().trim();
        if (!ValidacionUtils.esNumeroValido(textoPrecio)) {
            error.setText("⚠ 'precio' debe ser un número válido.");
            return;
        }

        double precio = Double.parseDouble(textoPrecio);
        double tasa = TasaCambioUtils.obtenerUltima();
        boolean esLocal = ((RadioButton) data.grupo.getToggles().get(0)).isSelected();

        double precioLocal = esLocal ? precio : precio * tasa;
        double precioDolar = esLocal ? precio / tasa : precio;

        datos.put("Código", codigo);
        datos.put("Nombre", nombre);
        datos.put("Unidad", unidad);
        datos.put("Precio Local", String.format("%.4f", precioLocal));
        datos.put("Precio Dólar", String.format("%.4f", precioDolar));
        datos.put("Fecha de actualización", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        datos.put("Categoria", categoria);

        AtomicBoolean modificado = new AtomicBoolean(false);

        Thread hilo = new Thread(() -> {
            double precioLocalAnt = obtenerPrecioLocalActual(tabla, codigo);

            boolean ok = ExcelUtils.modificarPorCodigo(tabla, codigo, datos);
            modificado.set(true);

            Platform.runLater(() -> {
                if (ok) {
                    error.setStyle("-fx-text-fill: green;");
                    error.setText("✅ Modificado correctamente.");
                    System.out.println("✏️ Modificado en " + tabla + ": " + datos);

                    Map<String, String> registroHistorial = new LinkedHashMap<>();
                    registroHistorial.put("Código", codigo);
                    registroHistorial.put("Ingrediente", nombre);
                    registroHistorial.put("Fecha de modificación", datos.get("Fecha de actualización"));
                    registroHistorial.put("Precio anterior", String.format("%.4f", precioLocalAnt));
                    registroHistorial.put("Precio nuevo", datos.get("Precio Local"));

                    ExcelUtils.agregarAFilaVacia("HistorialPrecios", registroHistorial);
                    System.out.println("📌 Historial guardado: " + registroHistorial);
                } else {
                    error.setStyle("-fx-text-fill: red;");
                    error.setText("❌ No se pudo modificar.");
                    TablaViewer.mostrarTabla(tabla);
                }
            });
        });

        hilo.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!modificado.get()) {
                    Platform.runLater(() -> {
                        error.setStyle("-fx-text-fill: red;");
                        error.setText("❌ La modificación demoró demasiado.");
                        TablaViewer.mostrarTabla(tabla);
                    });
                }
            }
        }, 3000);
    }

    private static String obtenerTexto(Node n) {
        if (n instanceof TextField tf) return tf.getText().trim();
        if (n instanceof ComboBox<?> cb && cb.getValue() != null) return cb.getValue().toString().trim();
        return "";
    }

    private static double obtenerPrecioLocalActual(String tabla, String codigo) {
        List<Map<String, String>> registros = ExcelDataUtils.obtenerTabla(tabla);
        for (Map<String, String> reg : registros) {
            if (codigo.equalsIgnoreCase(reg.get("Código"))) {
                try {
                    return Double.parseDouble(reg.getOrDefault("Precio Local", "0"));
                } catch (Exception e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
