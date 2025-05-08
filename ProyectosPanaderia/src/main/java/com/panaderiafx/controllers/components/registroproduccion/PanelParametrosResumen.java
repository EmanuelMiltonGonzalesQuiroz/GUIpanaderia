package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.cache.CacheGananciasUtils;
import com.panaderiafx.utils.cache.CacheParametrosUtils;
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

public class PanelParametrosResumen {

    public static Node crear() {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");

        Label titulo = new Label("PARÁMETROS");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> parametros = com.panaderiafx.utils.VerUtils.verTabla("Parametros");
        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList(parametros);

        for (Map<String, String> fila : datos) {
            fila.putIfAbsent("Check", "✓");
        }

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(300);

        TableColumn<Map<String, String>, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Nombre", "")));

        TableColumn<Map<String, String>, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Valor", "")));
        colValor.setCellFactory(tc -> new TableCell<>() {
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

            public void commitEdit(String newValue) {
                int index = getIndex();
                Map<String, String> fila = tabla.getItems().get(index);
                fila.put("Valor", String.format("%.2f", ParseUtils.toDouble(newValue)));
                recalcularDesdeTabla(tabla.getItems());
                tabla.refresh();
            }
        });

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidad", "")));

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setMinWidth(30);
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    String nuevoValor = actual.equals("✓") ? " " : "✓";
                    fila.put("Check", nuevoValor);

                    String codigo = fila.getOrDefault("Código", "");
                    if (codigo.equals("PAR0001") || codigo.equals("PAR0002")) {
                        for (Map<String, String> f : tabla.getItems()) {
                            if ((codigo.equals("PAR0001") && f.getOrDefault("Código", "").equals("PAR0002")) ||
                                (codigo.equals("PAR0002") && f.getOrDefault("Código", "").equals("PAR0001"))) {
                                f.put("Check", nuevoValor);
                            }
                        }
                    }

                    recalcularDesdeTabla(tabla.getItems());
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

        tabla.getColumns().addAll(colNombre, colValor, colUnidad, colCheck);
        contenedor.getChildren().addAll(titulo, tabla);

        recalcularDesdeTabla(tabla.getItems());
        return contenedor;
    }

    private static void recalcularDesdeTabla(List<Map<String, String>> parametros) {
        double total = 0.0;
        double ganancia = CacheGananciasUtils.get();

        double costoManoObra = 0.0;
        int empleados = 1;
        boolean activoManoObra = true;
        boolean activoEmpleados = true;

        for (Map<String, String> fila : parametros) {
            String codigo = fila.getOrDefault("Código", "").trim();
            String unidad = fila.getOrDefault("Unidad", "").trim().toLowerCase();
            boolean check = fila.getOrDefault("Check", "✓").equals("✓");
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));

            switch (codigo) {
                case "PAR0001" -> {
                    costoManoObra = valor;
                    activoManoObra = check;
                }
                case "PAR0002" -> {
                    empleados = (int) valor;
                    activoEmpleados = check;
                }
                default -> {
                    if (unidad.contains("%") && check) {
                        total += ganancia * (valor / 100.0);
                    }
                }
            }
        }

        if (activoManoObra && activoEmpleados) {
            total += costoManoObra * empleados;
        }

        CacheParametrosUtils.set(total);
    }
}
