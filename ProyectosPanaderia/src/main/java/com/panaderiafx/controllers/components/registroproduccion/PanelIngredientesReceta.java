package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class PanelIngredientesReceta {

    public static VBox crear(String codigoReceta) {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #FF9800; -fx-padding: 20; -fx-background-radius: 10;");
        panel.setPrefWidth(450);

        Label titulo = new Label("INGREDIENTES");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> ingredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> filtrados = ingredientes.stream()
                .filter(m -> m.getOrDefault("Código receta", "").equals(codigoReceta))
                .toList();

        if (filtrados.isEmpty()) {
            Label error = new Label("Receta no encontrada");
            error.setStyle("-fx-background-color: #FFD180; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");
            panel.getChildren().add(error);
            return panel;
        }

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();
        for (Map<String, String> fila : filtrados) {
            fila.put("Check", "✓");
            datos.add(fila);
        }

        // campos de totales
        TextField campoTotal = new TextField("0.00");
        campoTotal.setEditable(false);
        campoTotal.setPrefWidth(100);

        TextField campoUnitario = new TextField("0.00");
        campoUnitario.setEditable(false);
        campoUnitario.setPrefWidth(100);

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(250);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colIng = new TableColumn<>("Ingrediente");
        colIng.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Ingrediente", "")));

        TableColumn<Map<String, String>, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Cantidad", "")));

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidades", "")));

        TableColumn<Map<String, String>, String> colCosto = new TableColumn<>("Costo");
        colCosto.setCellValueFactory(f -> {
            String codIng = f.getValue().getOrDefault("Ingrediente", "");
            double cantProducida = obtenerCantidadProduccionActual(codigoReceta);
            double costo = CostoIngredientePorRecetaUtils.calcular(codigoReceta, codIng, cantProducida);
            f.getValue().put("Costo", String.format("%.2f", costo)); // guarda el valor por si se vuelve a usar
            return new SimpleStringProperty(String.format("%.2f", costo));
        });

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setMinWidth(60);
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    getTableView().refresh();
                    actualizarTotales(tabla, campoTotal, campoUnitario, codigoReceta);
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

        tabla.getColumns().addAll(colIng, colCant, colUnidad, colCosto, colCheck);

        Label lblCosto = new Label("COSTO TOTAL:");
        Label lblUnitario = new Label("COSTO x UNIDAD:");

        VBox totales = new VBox(5,
                new HBox(10, lblCosto, campoTotal),
                new HBox(10, lblUnitario, campoUnitario)
        );
        totales.setPadding(new Insets(10));
        totales.setStyle("-fx-background-color: #FFB74D;");

        actualizarTotales(tabla, campoTotal, campoUnitario, codigoReceta);

        panel.getChildren().addAll(titulo, tabla, totales);
        return panel;
    }

    private static double obtenerCantidadProduccionActual(String codReceta) {
        return VerUtils.verTabla("Produccion").stream()
                .filter(p -> p.getOrDefault("Código receta", "").equals(codReceta))
                .map(p -> ParseUtils.toDouble(p.getOrDefault("Cantidad producida", "0")))
                .reduce((a, b) -> b).orElse(0.0);
    }

    private static void actualizarTotales(TableView<Map<String, String>> tabla, TextField campoTotal, TextField campoUnitario, String codReceta) {
        double total = 0;
        for (Map<String, String> fila : tabla.getItems()) {
            if ("✓".equals(fila.getOrDefault("Check", "✓"))) {
                total += ParseUtils.toDouble(fila.getOrDefault("Costo", "0"));
            }
        }

        campoTotal.setText(String.format("%.2f", total));

        double cantidadProducida = obtenerCantidadProduccionActual(codReceta);
        double unitario = (cantidadProducida > 0) ? total / cantidadProducida : 0;
        campoUnitario.setText(String.format("%.4f", unitario));
    }
}
