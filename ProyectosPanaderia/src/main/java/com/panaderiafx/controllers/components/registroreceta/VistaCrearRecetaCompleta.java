package com.panaderiafx.controllers.components.registroreceta;

import com.panaderiafx.utils.CrearUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.util.List;
import java.util.Map;

public class VistaCrearRecetaCompleta {

    public static Node crearVista() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(30));
        contenedor.setStyle("-fx-background-color: #FFF3E0;");

        FormularioCabeceraReceta cabecera = new FormularioCabeceraReceta();
        TablaIngredientesEditable ingredientes = new TablaIngredientesEditable();

        Button btnGuardar = new Button("💾 Guardar receta");
        btnGuardar.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            Map<String, String> datosCabecera = cabecera.getDatos();
            List<Map<String, String>> datosIngredientes = ingredientes.getFilas();

            // Validación básica
            if (datosCabecera.getOrDefault("Producto", "").isBlank()
                    || datosCabecera.getOrDefault("Código receta", "").isBlank()
                    || datosCabecera.getOrDefault("Versión", "").isBlank()) {
                mostrarAlerta("Faltan datos importantes en la cabecera.");
                return;
            }

            if (datosIngredientes.isEmpty()) {
                mostrarAlerta("Debe ingresar al menos un ingrediente.");
                return;
            }

            // Guardar cabecera en Recetas
            CrearUtils.crearFila("Recetas", datosCabecera);

            // Guardar ingredientes en RecetasIngredientes
            for (Map<String, String> fila : datosIngredientes) {
                fila.put("Código receta", datosCabecera.get("Código receta"));
                fila.put("Receta", datosCabecera.get("Producto"));
                fila.put("Versión", datosCabecera.get("Versión"));
                CrearUtils.crearFila("RecetasIngredientes", fila);
            }

            mostrarConfirmacion("✅ Receta guardada correctamente.");
        });

        HBox filaBoton = new HBox(btnGuardar);
        filaBoton.setPadding(new Insets(10, 0, 0, 0));

        contenedor.getChildren().addAll(
                cabecera.getNode(),
                ingredientes.getNodeConBoton(),
                filaBoton
        );

        return contenedor;
    }

    private static void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
