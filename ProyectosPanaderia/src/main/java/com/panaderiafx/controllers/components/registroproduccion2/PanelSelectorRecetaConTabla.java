package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PanelSelectorRecetaConTabla extends VBox {

    private final DatePicker campoFecha = new DatePicker(LocalDate.now());
    private final TableView<Map<String, String>> tabla = new TableView<>();
    private Map<String, String> recetaSeleccionada = null;
    private Consumer<Map<String, String>> callbackCambio = null;

    public PanelSelectorRecetaConTabla() {
        setSpacing(10);
        setPadding(new Insets(10));

        // Fila para seleccionar fecha
        HBox filaFecha = new HBox(10, new Label("Fecha:"), campoFecha);

        // Columnas de tabla
        agregarColumna("Código receta", "Código receta");
        agregarColumna("Producto", "Producto");
        agregarColumna("Versión", "Versión");
        agregarColumna("Rendimiento", "Rendimiento");

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(500);

        tabla.setOnMouseClicked(event -> {
            Map<String, String> seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion != null && !seleccion.equals(recetaSeleccionada)) {
                recetaSeleccionada = seleccion;
                if (callbackCambio != null) callbackCambio.accept(seleccion);
            }
        });

        getChildren().addAll(filaFecha, tabla);
        recargar();
    }

    private void agregarColumna(String titulo, String campo) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault(campo, "")));
        tabla.getColumns().add(col);
    }

    public void recargar() {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        VerUtils.refrescarExcel();
        tabla.getItems().setAll(recetas);
    }

    public String getCodigoRecetaSeleccionado() {
        return recetaSeleccionada != null ? recetaSeleccionada.getOrDefault("Código receta", null) : null;
    }

    public String getFechaSeleccionada() {
        return campoFecha.getValue() != null ? campoFecha.getValue().toString() : "";
    }

    public void setOnRecetaSeleccionada(Consumer<Map<String, String>> callback) {
        this.callbackCambio = callback;
    }

    public Node getNodo() {
        return this;
    }
    
}
