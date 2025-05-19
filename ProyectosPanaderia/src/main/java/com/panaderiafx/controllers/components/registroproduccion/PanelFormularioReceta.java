package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.cache.RecetaCacheUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BiConsumer;

public class PanelFormularioReceta {

    public static Node crear(String codigoReceta, Map<String, String> prod,
                             BiConsumer<String, Double> actualizarGananciaEnTabla,
                             Runnable onCantidadActualizada) {

        VBox contenedor = new VBox(15);
        contenedor.setStyle("-fx-background-color: #F36C00; -fx-padding: 20; -fx-background-radius: 10;");
        contenedor.setAlignment(Pos.TOP_LEFT);

        String nombreProducto = RecetaCacheUtils.obtenerNombre(codigoReceta);

        if (prod == null || nombreProducto == null || nombreProducto.isBlank()) {
            Label error = new Label("Receta no encontrada");
            error.setStyle("-fx-background-color: #FFD180; -fx-padding: 10; -fx-border-radius: 5;");
            contenedor.getChildren().add(error);
            return contenedor;
        }

        double cantidad = parseDoubleSafe(prod.getOrDefault("Cantidad producida", "0"));
        double precioUnidad = parseDoubleSafe(prod.getOrDefault("Precio de Venta por Unidad", "0"));
        double precioGeneral = cantidad * precioUnidad;

        TextField campoNombre = new TextField(nombreProducto);
        campoNombre.setEditable(false);
        campoNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField campoCantidad = new TextField(String.format("%.0f", cantidad));
        TextField campoPrecioUnidad = new TextField(String.format("%.2f", precioUnidad));
        TextField campoPrecioTotal = new TextField(String.format("%.2f", precioGeneral));

        final boolean[] bloqueado = {false};

        Runnable recalculo = () -> {
            if (VistaGananciasCostosDirectos.recalcularTotales != null) {
                VistaGananciasCostosDirectos.recalcularTotales.run();
            }
        };

        Runnable actualizarDesdeCantidadYUnitario = () -> {
            if (bloqueado[0]) return;
            bloqueado[0] = true;

            double cant = parseDoubleSafe(campoCantidad.getText());
            double unit = parseDoubleSafe(campoPrecioUnidad.getText());
            double total = cant * unit;

            campoPrecioTotal.setText(String.format("%.2f", total));
            prod.put("Cantidad producida", String.format("%.0f", cant));
            prod.put("Precio de Venta por Unidad", String.valueOf(unit));
            prod.put("Precio de Venta General", String.valueOf(total));

            if (actualizarGananciaEnTabla != null) {
                actualizarGananciaEnTabla.accept(codigoReceta, total);
            }

            if (onCantidadActualizada != null) {
                onCantidadActualizada.run();
            }

            recalculo.run();
            bloqueado[0] = false;
        };

        Runnable actualizarDesdeTotal = () -> {
            if (bloqueado[0]) return;
            bloqueado[0] = true;

            double cant = parseDoubleSafe(campoCantidad.getText());
            double total = parseDoubleSafe(campoPrecioTotal.getText());
            double unit = cant != 0 ? total / cant : 0;

            campoPrecioUnidad.setText(String.format("%.2f", unit));
            prod.put("Cantidad producida", String.format("%.0f", cant));
            prod.put("Precio de Venta por Unidad", String.valueOf(unit));
            prod.put("Precio de Venta General", String.valueOf(total));

            if (actualizarGananciaEnTabla != null) {
                actualizarGananciaEnTabla.accept(codigoReceta, total);
            }

            if (onCantidadActualizada != null) {
                onCantidadActualizada.run();
            }

            recalculo.run();
            bloqueado[0] = false;
        };

        campoCantidad.textProperty().addListener((obs, o, n) -> actualizarDesdeCantidadYUnitario.run());
        campoPrecioUnidad.textProperty().addListener((obs, o, n) -> actualizarDesdeCantidadYUnitario.run());
        campoPrecioTotal.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) actualizarDesdeTotal.run();
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(crearEtiqueta("CANTIDAD PRODUCIDA"), 0, 0); grid.add(campoCantidad, 1, 0);
        grid.add(crearEtiqueta("PRECIO DE VENTA POR UNIDAD"), 0, 1); grid.add(campoPrecioUnidad, 1, 1);
        grid.add(crearEtiqueta("PRECIO DE VENTA GENERAL"), 0, 2); grid.add(campoPrecioTotal, 1, 2);

        contenedor.getChildren().addAll(campoNombre, grid);
        return contenedor;
    }

    private static Label crearEtiqueta(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-background-color: #FFC107; -fx-font-weight: bold; -fx-padding: 5 10;");
        return lbl;
    }

    private static double parseDoubleSafe(String val) {
        try {
            return Double.parseDouble(val.trim().replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
