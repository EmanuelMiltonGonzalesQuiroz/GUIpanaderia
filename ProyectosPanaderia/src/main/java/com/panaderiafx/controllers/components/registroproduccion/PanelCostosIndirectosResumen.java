package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosIndirectosUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PanelCostosIndirectosResumen {

    public static Node crear() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");

        Label titulo = new Label("COSTOS INDIRECTOS");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> costos = VerUtils.verTabla("Costos").stream()
                .filter(c -> c.getOrDefault("Tipo", "").equalsIgnoreCase("Indirecto"))
                .collect(Collectors.toList());

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList(costos);
        for (Map<String, String> fila : datos) {
            fila.putIfAbsent("Check", "✓");
        }

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(300);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Item", "")));

        TableColumn<Map<String, String>, String> colFrecuencia = new TableColumn<>("Frecuencia");
        colFrecuencia.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Frecuencia", "")));

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidad", "")));

        TableColumn<Map<String, String>, String> colPrecio = new TableColumn<>("Precio Local");
        colPrecio.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Precio Local", "0")));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            private final TextField field = new TextField();

            {
                field.setOnAction(e -> commitEdit(field.getText()));
                field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) commitEdit(field.getText());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    field.setText(item);
                    setGraphic(field);
                }
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue); // importante para notificar edición
                int index = getIndex();
                if (index >= 0 && index < tabla.getItems().size()) {
                    Map<String, String> fila = tabla.getItems().get(index);
                    fila.put("Precio Local", newValue);
                    CacheCostosIndirectosUtils.recalcular(tabla.getItems()); // ✅ Usar datos visibles
                    tabla.refresh();
                }
            }
        });

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setMinWidth(30);
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    CacheCostosIndirectosUtils.recalcular(tabla.getItems()); // ✅ Recalcular con los items actuales
                    tabla.refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    btn.setText(fila.getOrDefault("Check", "✓").equals("✓") ? "✓" : " ");
                    setGraphic(btn);
                }
            }
        });
        colCheck.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Check", "✓")));

        tabla.getColumns().addAll(colItem, colFrecuencia, colUnidad, colPrecio, colCheck);

        contenedor.getChildren().addAll(titulo, tabla);
        return contenedor;
    }
}
