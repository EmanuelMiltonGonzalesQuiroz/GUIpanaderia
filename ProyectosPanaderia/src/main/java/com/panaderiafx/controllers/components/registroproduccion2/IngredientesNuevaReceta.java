package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IngredientesNuevaReceta {

    private Supplier<String> cantidadActualSupplier;

    public Node crear(String codReceta, String versionReceta, Consumer<Double> actualizarCostoCallback, Supplier<String> cantidadSupplier) {
        this.cantidadActualSupplier = cantidadSupplier;

        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #FF9800; -fx-padding: 20; -fx-background-radius: 10;");

        Label titulo = new Label("INGREDIENTES");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> mapaNombre = ingredientes.stream()
                .collect(Collectors.toMap(
                        i -> i.getOrDefault("Código", "").trim(),
                        i -> i.getOrDefault("Nombre", "").trim()
                ));

        List<Map<String, String>> filtrados = recetasIngredientes.stream()
                .filter(m -> m.getOrDefault("Código receta", "").equals(codReceta)
                        && m.getOrDefault("Versión", "").equals(versionReceta))
                .collect(Collectors.toList());

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();
        for (Map<String, String> fila : filtrados) {
            fila.put("Check", "✓");
            String codIng = fila.getOrDefault("Ingrediente", "");
            double costo = CostoIngredientePorRecetaUtils.calcular(codReceta, codIng, 1);
            fila.put("Costo", String.format("%.2f", costo));
            datos.add(fila);
        }

        TableView<Map<String, String>> tabla = new TableView<>(datos); 
        tabla.setPrefHeight(300);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colIng = new TableColumn<>("Ingrediente");
        colIng.setCellValueFactory(f -> {
            String cod = f.getValue().getOrDefault("Ingrediente", "");
            return new SimpleStringProperty(mapaNombre.getOrDefault(cod, cod));
        });

        TableColumn<Map<String, String>, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellFactory(col -> new TableCell<>() {
            private final TextField editor = new TextField();

            {
                editor.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    fila.put("Cantidad", editor.getText());
                    getTableView().refresh();
                    recalcularCosto(tabla.getItems(), codReceta, versionReceta, actualizarCostoCallback);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    editor.setText(fila.getOrDefault("Cantidad", ""));
                    setGraphic(editor);
                }
            }
        });

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidades", "")));

        TableColumn<Map<String, String>, String> colCosto = new TableColumn<>("Costo");
        colCosto.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Costo", "0.00")));

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button();
            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    getTableView().refresh();
                    recalcularCosto(tabla.getItems(), codReceta, versionReceta, actualizarCostoCallback);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    btn.setText(fila.getOrDefault("Check", "✓").equals("✓") ? "✓" : " ");
                    setGraphic(btn);
                }
            }
        });

        tabla.getColumns().addAll(colIng, colCant, colUnidad, colCosto, colCheck);
        panel.getChildren().addAll(titulo, tabla);

        // Cálculo inicial
        recalcularCosto(tabla.getItems(), codReceta, versionReceta, actualizarCostoCallback);

        return panel;
    }

    private void recalcularCosto(List<Map<String, String>> datos, String codReceta, String version, Consumer<Double> callback) {
        double totalIngredientes = datos.stream()
                .filter(f -> "✓".equals(f.getOrDefault("Check", "✓")))
                .mapToDouble(f -> {
                    String codIng = f.getOrDefault("Ingrediente", "");
                    double cantidad = ParseUtils.toDouble(f.getOrDefault("Cantidad", "1"));
                    return CostoIngredientePorRecetaUtils.calcular(codReceta, codIng, cantidad);
                }).sum();

        double rendimiento = VerUtils.verTabla("Recetas").stream()
                .filter(r -> r.getOrDefault("Código receta", "").equals(codReceta)
                        && r.getOrDefault("Versión", "").equals(version))
                .map(r -> ParseUtils.toDouble(r.getOrDefault("Rendimiento", "0")))
                .findFirst().orElse(0.0);

        double cantidadProduccion = cantidadActualSupplier != null
                ? ParseUtils.toDouble(cantidadActualSupplier.get())
                : 0;

        double costoUnitario = (rendimiento > 0) ? totalIngredientes / rendimiento : 0;
        double costoTotal = costoUnitario * cantidadProduccion;

        if (callback != null) callback.accept(costoTotal);
    }
}
