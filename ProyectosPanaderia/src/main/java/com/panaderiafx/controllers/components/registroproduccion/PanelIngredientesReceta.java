package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PanelIngredientesReceta {

    private static TableView<Map<String, String>> tablaActual = null;
    private static TextField campoTotalActual = null;
    private static TextField campoUnitarioActual = null;
    private static String produccionActual = null;
    private static Map<String, String> prodActual = null;
    private static BiConsumer<String, Double> actualizadorActual = null;

    public static VBox crear(String codigoReceta, Map<String, String> prod, String codigoProduccion, BiConsumer<String, Double> actualizarCostoEnTabla) {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #FF9800; -fx-padding: 20; -fx-background-radius: 10;");
        panel.setPrefWidth(450);

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
                .filter(m -> m.getOrDefault("Código receta", "").trim().equalsIgnoreCase(codigoReceta.trim()))
                .toList();

        if (filtrados.isEmpty()) {
            System.out.println("❌ No se encontraron ingredientes para la receta: " + codigoReceta);
            Label error = new Label("Receta no encontrada");
            error.setStyle("-fx-background-color: #FFD180; -fx-padding: 10; -fx-border-radius: 5;");
            panel.getChildren().add(error);
            return panel;
        }

        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();
        for (Map<String, String> fila : filtrados) {
            fila.put("Check", "✓");
            String codIng = fila.getOrDefault("Ingrediente", "");
            double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "1"));
            double costo = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, fila.getOrDefault("Unidades", ""), cantidad);
            fila.put("Costo", String.format("%.2f", costo));
            datos.add(fila);
        }

        TextField campoTotal = new TextField(prod.getOrDefault("Costo directo", "0.00"));
        campoTotal.setEditable(false);
        campoTotal.setPrefWidth(100);

        TextField campoUnitario = new TextField(prod.getOrDefault("Costo/U", "0.0000"));
        campoUnitario.setEditable(false);
        campoUnitario.setPrefWidth(100);

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(250);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colIng = new TableColumn<>("Ingrediente");
        colIng.setMinWidth(100);
        colIng.setCellValueFactory(f -> {
            String cod = f.getValue().getOrDefault("Ingrediente", "");
            return new SimpleStringProperty(mapaNombre.getOrDefault(cod, cod));
        });

        TableColumn<Map<String, String>, String> colCant = new TableColumn<>("Cantidad");
        colCant.setCellFactory(col -> new TableCell<>() {
            private final TextField editor = new TextField();

            {
                editor.setOnAction(e -> actualizarCantidad());
                editor.focusedProperty().addListener((obs, old, focus) -> {
                    if (!focus) actualizarCantidad();
                });
            }

            private void actualizarCantidad() {
                Map<String, String> fila = getTableView().getItems().get(getIndex());
                String cantidadStr = editor.getText();
                fila.put("Cantidad", cantidadStr);
                double cantidad = ParseUtils.toDouble(cantidadStr);
                String codIng = fila.getOrDefault("Ingrediente", "");
                String unidad = fila.getOrDefault("Unidades", "");
                double nuevoCosto = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, unidad, cantidad);
                fila.put("Costo", String.format("%.2f", nuevoCosto));
                getTableView().refresh();
                actualizarTotales(getTableView().getItems(), campoTotalActual, campoUnitarioActual, produccionActual, prodActual, actualizadorActual);
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    editor.setText(fila.getOrDefault("Cantidad", ""));
                    setGraphic(editor);
                }
            }
        });
        colCant.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Cantidad", "")));

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Unidades", "")));

        TableColumn<Map<String, String>, String> colCosto = new TableColumn<>("Costo");
        colCosto.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Costo", "0.00")));

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("✓");
        colCheck.setMinWidth(30);
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    getTableView().refresh();
                    actualizarTotales(getTableView().getItems(), campoTotalActual, campoUnitarioActual, produccionActual, prodActual, actualizadorActual);
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

        tablaActual = tabla;
        campoTotalActual = campoTotal;
        campoUnitarioActual = campoUnitario;
        produccionActual = codigoProduccion;
        prodActual = prod;
        actualizadorActual = actualizarCostoEnTabla;

        actualizarTotales(datos, campoTotal, campoUnitario, codigoProduccion, prod, actualizarCostoEnTabla);

        panel.getChildren().addAll(titulo, tabla, totales);
        return panel;
    }

    private static double obtenerRendimientoDesdeDatosActuales(Map<String, String> prod) {
        double r = ParseUtils.toDouble(prod.getOrDefault("Rendimiento", "0"));
        if (r > 0) return r;

        String cod = prod.getOrDefault("Código receta", "").trim();
        if (cod.isEmpty()) return 0;

        return VerUtils.verTabla("Recetas").stream()
                .filter(p -> cod.equalsIgnoreCase(p.getOrDefault("Código receta", "").trim()))
                .map(p -> ParseUtils.toDouble(p.getOrDefault("Rendimiento", "0")))
                .findFirst().orElse(0.0);
    }

    private static void actualizarTotales(List<Map<String, String>> datos, TextField campoTotal, TextField campoUnitario,
                                          String codProduccion, Map<String, String> prod, BiConsumer<String, Double> actualizarCostoEnTabla) {
        double total = 0;
        for (Map<String, String> fila : datos) {
            if (!"✓".equals(fila.getOrDefault("Check", "✓"))) continue;

            String codIng = fila.getOrDefault("Ingrediente", "");
            double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "1"));
            String unidad = fila.getOrDefault("Unidades", "");
            double costo = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, unidad, cantidad);
            fila.put("Costo", String.format("%.2f", costo));
            total += costo;
        }

        double rendimiento = obtenerRendimientoDesdeDatosActuales(prod);
        double costoUnitario = (rendimiento > 0) ? total / rendimiento : 0;
        double cantidadProducida = ParseUtils.toDouble(prod.getOrDefault("Cantidad producida", "0"));
        double costoTotalFinal = costoUnitario * cantidadProducida;

        prod.put("Costo directo", String.format("%.2f", costoTotalFinal));
        prod.put("Costo/U", String.format("%.2f", costoUnitario));

        campoTotal.setText(String.format("%.2f", costoTotalFinal));
        campoUnitario.setText(String.format("%.4f", costoUnitario));

        CacheCostosDirectosUtils.editar(codProduccion, costoTotalFinal);
        if (actualizadorActual != null) {
            actualizadorActual.accept(codProduccion, costoTotalFinal);
        }

        if (VistaGananciasCostosDirectos.recalcularTotales != null) {
            VistaGananciasCostosDirectos.recalcularTotales.run();
        }
    }

    public static void forzarRecalculo() {
        if (tablaActual != null && campoTotalActual != null && campoUnitarioActual != null &&
                produccionActual != null && prodActual != null) {
            actualizarTotales(tablaActual.getItems(), campoTotalActual, campoUnitarioActual, produccionActual, prodActual, actualizadorActual);
        }
    }
}
