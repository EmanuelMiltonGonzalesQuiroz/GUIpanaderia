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

public class PanelDetalleParametros {

    public static Node crear() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FFD54F; -fx-background-radius: 10;");

        Label titulo = new Label("DETALLE PARÁMETROS");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> datosOriginales = VerUtils.verTabla("Parametros");
        for (Map<String, String> fila : datosOriginales) {
            fila.putIfAbsent("Check", "✓");
        }

        // Aplicar cambios guardados en cache si existen
        EditorTemporalCache.aplicarCache("Parametros", "Código", datosOriginales);

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList(datosOriginales);

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(300);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Nombre", "")));

        TableColumn<Map<String, String>, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Valor", "0")));
        colValor.setCellFactory(tc -> new TableCell<>() {
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
                    fila.put("Valor", newValue);
                    EditorTemporalCache.guardarFila("Parametros", fila.get("Código"), fila);
                    recalcularParametros(tabla.getItems());
                    tabla.refresh();
                }
            }
        });

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidad", "")));

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
                    EditorTemporalCache.guardarFila("Parametros", fila.get("Código"), fila);
                    recalcularParametros(tabla.getItems());
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

        tabla.getColumns().addAll(colNombre, colValor, colUnidad, colCheck);
        contenedor.getChildren().addAll(titulo, tabla);
        recalcularParametros(tabla.getItems());
        return contenedor;
    }

    private static void recalcularParametros(List<Map<String, String>> filas) {
        double total = 0;
        double ganancia = CacheLibroSemanal.get(Tipo.GANANCIA_B);
        double manoObra = 0;
        int empleados = 1;
        boolean activoManoObra = false;
        boolean activoEmpleados = false;

        for (Map<String, String> fila : filas) {
            if (!"✓".equals(fila.getOrDefault("Check", ""))) continue;

            String cod = fila.getOrDefault("Código", "");
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
            String unidad = fila.getOrDefault("Unidad", "").toLowerCase();

            switch (cod) {
                case "PAR0001" -> {
                    manoObra = valor;
                    activoManoObra = true;
                }
                case "PAR0002" -> {
                    empleados = (int) valor;
                    activoEmpleados = true;
                }
                default -> {
                    if (unidad.contains("%")) {
                        double descuento = ganancia * (valor / 100.0);
                        total += descuento;
                    }
                }
            }
        }

        if (activoEmpleados && activoManoObra) {
            total += manoObra * empleados;
        }

        CacheLibroSemanal.set(Tipo.PARAMETROS, total);
    }
}
