package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import com.panaderiafx.utils.componentes.FechaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;
import com.panaderiafx.utils.cache.CacheGananciasUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TablaProduccionesFactory {

    public static TableView<Map<String, String>> ultimaTablaGenerada;

    public static TableView<Map<String, String>> crearTabla(
            String fecha,
            String tipo,
            BiConsumer<String, Map<String, String>> accionEditar,
            BiConsumer<Double, Double> actualizarTotales,
            Consumer<List<Map<String, String>>> exponerDatos
    ) {
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion").stream()
                .filter(p -> FechaUtils.coincide(p.get("Fecha"), fecha, tipo))
                .collect(Collectors.toList());

        if (exponerDatos != null) {
            exponerDatos.accept(produccion);
        }

        Map<String, String> mapaCodANombre = VerUtils.verTabla("Recetas").stream()
                .collect(Collectors.toMap(
                        r -> r.getOrDefault("Código receta", "").trim(),
                        r -> r.getOrDefault("Producto", "").trim(),
                        (a, b) -> a
                ));

        for (Map<String, String> fila : produccion) {
            fila.put("Check", "✓");

            String codReceta = fila.getOrDefault("Código receta", "");
            double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            double costoUnitario = CostoIngredientePorRecetaUtils.calcularUnitarioDesdeReceta(codReceta);
            double costoTotal = costoUnitario * cantidad;

            fila.put("Costo directo", String.format("%.2f", costoTotal));
            fila.put("Costo/U", String.format("%.2f", costoUnitario));
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

        TableColumn<Map<String, String>, String> colCantidad = new TableColumn<>("CANTIDAD");
        colCantidad.setMinWidth(100);
        colCantidad.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Cantidad producida", "0")));

        TableColumn<Map<String, String>, String> colGanancia = new TableColumn<>("GANANCIA B.");
        colGanancia.setMinWidth(100);
        colGanancia.setCellValueFactory(f -> {
            double cant = ParseUtils.toDouble(f.getValue().getOrDefault("Cantidad producida", "0"));
            String precioUraw = f.getValue().getOrDefault("Precio de Venta por Unidad", "0");
            double precioU = ParseUtils.toDouble(precioUraw);
            return new SimpleStringProperty(String.format("%.2f", cant * precioU));
        });

        TableColumn<Map<String, String>, String> colCosto = new TableColumn<>("COSTO R.");
        colCosto.setMinWidth(100);
        colCosto.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Costo directo", "0.00")));

        TableColumn<Map<String, String>, String> colCostoUnitario = new TableColumn<>("COSTO/U");
        colCostoUnitario.setMinWidth(80);
        colCostoUnitario.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Costo/U", "0.00")));

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
                    accionEditar.accept(cod, datos);
                    tabla.refresh();
                    recalcular(tabla.getItems(), actualizarTotales);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colProducto, colFecha, colCantidad, colGanancia, colCosto, colCostoUnitario, colCheck, colEditar);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(300);

        ultimaTablaGenerada = tabla;
        recalcular(items, actualizarTotales);
        return tabla;
    }

    public static void recalcular(List<Map<String, String>> datos, BiConsumer<Double, Double> actualizar) {
        double sumaGanancia = 0;
        double sumaCosto = 0;

        for (Map<String, String> fila : datos) {
            if (!"✓".equals(fila.getOrDefault("Check", "✓"))) continue;

            double cant = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            double precioU = ParseUtils.toDouble(fila.getOrDefault("Precio de Venta por Unidad", "0"));
            double ganancia = cant * precioU;

            double costoTotal = ParseUtils.toDouble(fila.getOrDefault("Costo directo", "0"));
            sumaGanancia += ganancia;
            sumaCosto += costoTotal;
        }

        CacheGananciasUtils.set(sumaGanancia);
        CacheCostosDirectosUtils.set(sumaCosto);
        actualizar.accept(sumaGanancia, sumaCosto);
    }
}
