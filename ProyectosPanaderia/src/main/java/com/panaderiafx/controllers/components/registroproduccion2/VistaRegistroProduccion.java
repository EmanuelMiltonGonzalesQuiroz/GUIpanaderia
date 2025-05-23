package com.panaderiafx.controllers.components.registroproduccion2;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class VistaRegistroProduccion {

    public static Node crearVista() {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 30;");

        Label titulo = new Label("Registrar Nueva Producción");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        PanelSelectorRecetaConTabla selector = new PanelSelectorRecetaConTabla();
        selector.setPrefWidth(600);
        selector.setPrefHeight(400);

        FormularioNuevaReceta formExtra = new FormularioNuevaReceta();
        VBox panelFormularioReceta = new VBox();
        VBox panelIngredientesReceta = new VBox();
        VBox columnaDerecha = new VBox(20, panelFormularioReceta, panelIngredientesReceta);
        columnaDerecha.setPrefWidth(500);
        VBox.setVgrow(panelFormularioReceta, Priority.ALWAYS);
        VBox.setVgrow(panelIngredientesReceta, Priority.ALWAYS);

        HBox detalle = new HBox(40, selector, columnaDerecha);
        HBox.setHgrow(selector, Priority.NEVER);
        HBox.setHgrow(columnaDerecha, Priority.ALWAYS);

        Button btnGuardar = new Button("💾 GUARDAR PRODUCCIÓN");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnActualizar = new Button("🔁 ACTUALIZAR");
        btnActualizar.setStyle("-fx-background-color: #FFA726; -fx-text-fill: black; -fx-font-weight: bold;");
        btnActualizar.setOnAction(e -> selector.recargar());

        HBox filaBotones = new HBox(10, btnGuardar, btnActualizar);

        btnGuardar.setOnAction(e -> {
            String codReceta = selector.getCodigoRecetaSeleccionado();
            String fecha = selector.getFechaSeleccionada();
            String cantidad = formExtra.getCantidad();
            String precioU = formExtra.getPrecioUnitario();
            String total = formExtra.getPrecioTotal();
            String mezcla = formExtra.getMezclas();

            if (codReceta == null || fecha.isEmpty() || cantidad.isEmpty() || precioU.isEmpty() || total.isEmpty()) {
                mostrarError("Complete todos los campos antes de guardar.");
                return;
            }

            try {
                if (Double.parseDouble(cantidad) == 0 || Double.parseDouble(precioU) == 0 || Double.parseDouble(total) == 0) {
                    mostrarError("Cantidad, precio unitario y total deben ser mayores a cero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarError("Los valores numéricos no son válidos.");
                return;
            }

            GuardarProduccionUtils.guardar(codReceta, fecha, cantidad, precioU, total, mezcla);
            mostrarConfirmacion("✅ Producción guardada correctamente.");
        });

        selector.setOnRecetaSeleccionada(filaCompleta -> {
            String codReceta = filaCompleta.getOrDefault("Código receta", null);
            String version = filaCompleta.getOrDefault("Versión", "-");
            String rendimiento = filaCompleta.getOrDefault("Rendimiento", "-");
            String nombreProducto = filaCompleta.getOrDefault("Producto", codReceta);
            if (codReceta == null) return;

            formExtra.setCodigoReceta(codReceta);
            Node nodoFormulario = formExtra.crear(nombreProducto, version, rendimiento);
            formExtra.setCantidad("0");
            formExtra.setPrecioUnitario("0");
            formExtra.setPrecioTotal("0");

            panelFormularioReceta.getChildren().setAll(nodoFormulario);
        });

        contenedor.getChildren().addAll(titulo, detalle, filaBotones);
        return contenedor;
    }

    private static void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static void mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
