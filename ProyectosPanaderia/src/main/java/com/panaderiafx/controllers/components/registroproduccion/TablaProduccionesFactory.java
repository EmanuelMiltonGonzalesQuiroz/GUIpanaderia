package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostosDirectosPorRecetaUtils;
import com.panaderiafx.utils.componentes.FechaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TablaProduccionesFactory {

    public static TableView<Map<String, String>> crearTabla(String fecha, String tipo, Consumer<String> accionEditar, BiConsumer<Double, Double> actualizarTotales) {
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion").stream()
                .filter(p -> FechaUtils.coincide(p.get("Fecha"), fecha, tipo))
                .collect(Collectors.toList());

        Map<String, String> mapaCodANombre = VerUtils.verTabla("Recetas").stream()
                .collect(Collectors.toMap(
                        r -> r.getOrDefault("Código receta", "").trim(),
                        r -> r.getOrDefault("Producto", "").trim(),
                        (a, b) -> a
                ));

        for (Map<String, String> fila : produccion) {
            fila.put("Check", "✓");
        }

        ObservableList<Map<String, String>> items = FXCollections.observableArrayList(produccion);

        TableView<Map<String, String>> tabla = new TableView<>(items);

        TableColumn<Map<String, String>, String> colProducto = new TableColumn<>("RECETA");
        colProducto.setMinWidth(150);
        colProducto.setCellValueFactory(f -> {
            String cod = f.getValue().getOrDefault("Código receta", "");
            return new SimpleStringProperty(mapaCodANombre.getOrDefault(cod, cod));
        });

        TableColumn<Map<String, String>, String> colFecha = new TableColumn<>("FECHA");
        colFecha.setMinWidth(100);
        colFecha.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Fecha", "")));

        TableColumn<Map<String, String>, String> colGanancia = new TableColumn<>("GANANCIA");
        colGanancia.setMinWidth(100);
        colGanancia.setCellValueFactory(f -> {
            double cant = ParseUtils.toDouble(f.getValue().getOrDefault("Cantidad producida", "0"));
            double precioU = ParseUtils.toDouble(f.getValue().getOrDefault("Precio de Venta por Unidad", "0"));
            return new SimpleStringProperty(String.format("%.2f", cant * precioU));
        });

        TableColumn<Map<String, String>, String> colCosto = new TableColumn<>("COSTO");
        colCosto.setMinWidth(100);
        colCosto.setCellValueFactory(f -> {
            String cod = f.getValue().getOrDefault("Código receta", "");
            double cant = ParseUtils.toDouble(f.getValue().getOrDefault("Cantidad producida", "0"));
            if (CacheCostosDirectosUtils.contiene(cod, cant)) {
                return new SimpleStringProperty(String.format("%.2f", CacheCostosDirectosUtils.obtener(cod, cant)));
            }
            double costo = CostosDirectosPorRecetaUtils.calcular(cod, cant);
            CacheCostosDirectosUtils.guardar(cod, cant, costo);
            return new SimpleStringProperty(String.format("%.2f", costo));
        });

        TableColumn<Map<String, String>, String> colCheck = new TableColumn<>("CHECK");
        colCheck.setMinWidth(60);
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    getTableView().refresh();
                    recalcular(items, actualizarTotales);
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

        TableColumn<Map<String, String>, Void> colEditar = new TableColumn<>("EDITAR");
        colEditar.setMinWidth(90);
        colEditar.setCellFactory(param -> new TableCell<>() {
            final Button btn = new Button("EDITAR");

            {
                btn.setOnAction(event -> {
                    Map<String, String> datos = getTableView().getItems().get(getIndex());
                    String cod = datos.getOrDefault("Código receta", "");
                    accionEditar.accept(cod);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colProducto, colFecha, colGanancia, colCosto, colCheck, colEditar);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(300);

        recalcular(items, actualizarTotales);
        return tabla;
    }

    private static void recalcular(List<Map<String, String>> datos, BiConsumer<Double, Double> actualizar) {
        double sumaGanancia = 0;
        double sumaCosto = 0;

        for (Map<String, String> fila : datos) {
            if (!"✓".equals(fila.getOrDefault("Check", "✓"))) continue;

            double cant = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            double precioU = ParseUtils.toDouble(fila.getOrDefault("Precio de Venta por Unidad", "0"));
            double ganancia = cant * precioU;

            String cod = fila.getOrDefault("Código receta", "");
            double costo = CacheCostosDirectosUtils.contiene(cod, cant)
                    ? CacheCostosDirectosUtils.obtener(cod, cant)
                    : CostosDirectosPorRecetaUtils.calcular(cod, cant);

            CacheCostosDirectosUtils.guardar(cod, cant, costo);

            sumaGanancia += ganancia;
            sumaCosto += costo;
        }

        actualizar.accept(sumaGanancia, sumaCosto);
    }
}
