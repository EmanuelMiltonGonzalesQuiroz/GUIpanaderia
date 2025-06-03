package com.panaderiafx.controllers.components.registroreceta;

import com.panaderiafx.utils.CrearUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VistaCrearRecetaCompleta {

    public static Node crearVista() {
        BorderPane contenedor = new BorderPane();
        contenedor.setPadding(new Insets(30));
        contenedor.setStyle("-fx-background-color: #FFF3E0;");

        FormularioCabeceraReceta cabecera = new FormularioCabeceraReceta();
        TablaIngredientesEditable ingredientes = new TablaIngredientesEditable();
        ingredientes.agregarFilaVacia();

        Button btnGuardar = new Button("💾 Guardar receta");
        btnGuardar.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> {
            Map<String, String> datosCabecera = cabecera.getDatos();
            List<Map<String, String>> datosIngredientes = ingredientes.getFilas();

            // Validar datos de cabecera
            for (Map.Entry<String, String> entry : datosCabecera.entrySet()) {
                String key = entry.getKey();
                String val = Optional.ofNullable(entry.getValue()).orElse("").trim();

                if ((key.equals("Unidades por Molde") || key.equals("Molde/Paquete")) && val.isEmpty()) {
                    datosCabecera.put(key, "-");
                } else if (key.equals("Observaciones") && val.isEmpty()) {
                    datosCabecera.put(key, "Ninguna");
                } else if (!key.equals("Unidades por Molde") && !key.equals("Molde/Paquete") && val.isEmpty()) {
                    mostrarAlerta("El campo \"" + key + "\" no puede estar vacío.");
                    return;
                }
            }

            // Filtrar solo los ingredientes válidos
            List<Map<String, String>> filasValidas = datosIngredientes.stream()
                    .filter(f -> !f.getOrDefault("Ingrediente", "").isBlank()
                            && !f.getOrDefault("Cantidad", "").isBlank()
                            && !f.getOrDefault("Unidades", "").isBlank())
                    .collect(Collectors.toList());

            if (filasValidas.isEmpty()) {
                mostrarAlerta("Debe ingresar al menos un ingrediente válido.");
                return;
            }

            try {
                CrearUtils.crearFila("Recetas", datosCabecera);
                for (Map<String, String> fila : filasValidas) {
                    fila.put("Código receta", datosCabecera.get("Código receta"));
                    fila.put("Receta", datosCabecera.get("Producto"));
                    fila.put("Versión", datosCabecera.get("Versión"));
                    CrearUtils.crearFila("RecetasIngredientes", fila);
                }

                mostrarConfirmacion("✅ Receta guardada correctamente.");
                cabecera.limpiarCampos();
                ingredientes.limpiarTabla();

            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlerta("❌ Error al guardar la receta:\n" + ex.getMessage());
            }
        });

        HBox seccionCentro = new HBox(40);
        seccionCentro.setPadding(new Insets(0, 0, 20, 0));
        Node panelIngredientes = ingredientes.getNodeConBoton();
        seccionCentro.getChildren().addAll(cabecera.getNode(), panelIngredientes);
        HBox.setHgrow(panelIngredientes, Priority.ALWAYS);

        VBox centroYBoton = new VBox(20, seccionCentro, btnGuardar);
        contenedor.setCenter(centroYBoton);

        return contenedor;
    }

    private static void mostrarAlerta(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    private static void mostrarConfirmacion(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
}