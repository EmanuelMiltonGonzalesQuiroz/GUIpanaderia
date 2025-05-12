package com.panaderiafx.controllers.components.registroproduccion2;

import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

public class BotonGuardarProduccion extends HBox {

    public BotonGuardarProduccion(PanelSelectorRecetaConTabla selector, FormularioNuevaReceta formExtra) {
        setPadding(new Insets(10));
        setSpacing(10);

        Button btnGuardar = new Button("Guardar Producción");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        btnGuardar.setOnAction(e -> {
            String codReceta = selector.getCodigoRecetaSeleccionado();
            String fecha = selector.getFechaSeleccionada();

            if (codReceta == null || fecha.isEmpty()) {
                mostrarError("Debe seleccionar una receta y una fecha.");
                return;
            }

            String cantidad = formExtra.getCantidad();
            String precioU = formExtra.getPrecioUnitario();
            String total = formExtra.getPrecioTotal();

            if (cantidad.isEmpty() || precioU.isEmpty()) {
                mostrarError("Debe ingresar cantidad y precio por unidad.");
                return;
            }

            GuardarProduccionUtils.guardar(codReceta, fecha, cantidad, precioU, total);
            mostrarConfirmacion("Producción registrada correctamente.");
        });

        getChildren().add(btnGuardar);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
