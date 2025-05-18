package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import com.panaderiafx.utils.cache.EditorTemporalCache;
import com.panaderiafx.utils.componentes.ParseUtils;
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

public class PanelDetalleCostosIndirectos {

    public static Node crear() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FFE082; -fx-background-radius: 10;");

        Label titulo = new Label("DETALLE COSTOS INDIRECTOS");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> datosOriginales = VerUtils.verTabla("Costos").stream()
                .filter(f -> f.getOrDefault("Tipo", "").equalsIgnoreCase("Indirecto"))
                .collect(Collectors.toList());

        for (Map<String, String> fila : datosOriginales) {
            fila.putIfAbsent("Check", "✓");
        }

        // Aplicar cache de edición si existiera
        EditorTemporalCache.aplicarCache("Costos", "Código", datosOriginales);

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList(datosOriginales);

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(300);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Item", "")));

        TableColumn<Map<String, String>, String> colPrecio = new TableColumn<>("Precio Local");
        colPrecio.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Precio Local", "0")));
        colPrecio.setCellFactory(tc -> new TableCell<>() {
            private final TextField field = new TextField();

            {
                field.setOnAction(e -> commitEdit(field.getText()));
                field.focusedProperty().addListener((obs, old, nuevo) -> {
                    if (!nuevo) commitEdit(field.getText());
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
                super.commitEdit(newValue);
                int index = getIndex();
                if (index >= 0 && index < tabla.getItems().size()) {
                    Map<String, String> fila = tabla.getItems().get(index);
                    fila.put("Precio Local", newValue);
                    EditorTemporalCache.guardarFila("Costos", fila.get("Código"), fila);
                    recalcularDesdeTabla(tabla.getItems());
                    tabla.refresh();
                }
            }
        });

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setMinWidth(30);
        colCheck.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Check", "✓")));
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    EditorTemporalCache.guardarFila("Costos", fila.get("Código"), fila);
                    recalcularDesdeTabla(getTableView().getItems());
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

        tabla.getColumns().addAll(colItem, colPrecio, colCheck);
        contenedor.getChildren().addAll(titulo, tabla);
        recalcularDesdeTabla(tabla.getItems());
        return contenedor;
    }

    private static void recalcularDesdeTabla(List<Map<String, String>> filas) {
        double total = 0.0;
        for (Map<String, String> fila : filas) {
            if (!"✓".equals(fila.getOrDefault("Check", ""))) continue;

            double valor = ParseUtils.toDouble(fila.getOrDefault("Precio Local", "0"));
            String frecuencia = fila.getOrDefault("Frecuencia", "").toLowerCase();

            switch (frecuencia) {
                case "diario" -> total += valor * 7;
                case "semanal" -> total += valor;
                case "mensual" -> total += valor / 4.0;
                default -> total += valor;
            }
        }
        CacheLibroSemanal.set(Tipo.COSTO_INDIRECTO, total);
    }
}
