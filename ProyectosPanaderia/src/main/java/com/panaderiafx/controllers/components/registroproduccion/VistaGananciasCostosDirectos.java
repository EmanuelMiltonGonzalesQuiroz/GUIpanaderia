package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;

import java.util.Map;
import java.util.function.BiConsumer;

public class VistaGananciasCostosDirectos {

    public static Runnable recalcularTotales = null;

    public static Node crear(
            String fecha,
            String tipo,
            BiConsumer<String, Map<String, String>> abrirFormularioReceta,
            BiConsumer<Double, Double> actualizarTotales) {

        Label ganLab = new Label("TOTAL GANANCIA B.");
        ganLab.setStyle("-fx-font-weight: bold;");
        TextField ganField = new TextField("0.00");
        ganField.setEditable(false);

        Label cosLab = new Label("TOTAL COSTOS RECETAS");
        cosLab.setStyle("-fx-font-weight: bold;");
        TextField cosField = new TextField("0.00");
        cosField.setEditable(false);

        HBox resumen = new HBox(10, ganLab, ganField, cosLab, cosField);
        resumen.setPadding(new Insets(10));
        resumen.setStyle("-fx-background-color: #FFA726;");

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        contenedor.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");
        contenedor.setFillWidth(true);

        Label titulo = new Label("GANANCIAS BRUTAS Y COSTOS DE RECETAS");
        titulo.setStyle("-fx-font-weight: bold;");
        contenedor.getChildren().addAll(titulo);

        final TableView<Map<String, String>>[] tablaRef = new TableView[1];

        BiConsumer<Double, Double> actualizarCampos = (gan, cos) -> {
            ganField.setText(String.format("%.2f", gan));
            cosField.setText(String.format("%.2f", cos));
            actualizarTotales.accept(gan, cos);
        };

        Node tabla = TablaProduccionesFactory.crearTabla(
            fecha,
            tipo,
            (codigo, fila) -> {
                abrirFormularioReceta.accept(codigo, fila);
                if (tablaRef[0] != null) {
                    TablaProduccionesFactory.recalcular(tablaRef[0].getItems(), actualizarCampos);
                    tablaRef[0].refresh();
                }
            },
            actualizarCampos,
            lista -> {}
        );

        if (tabla instanceof TableView) {
            tablaRef[0] = (TableView<Map<String, String>>) tabla;
            TablaProduccionesFactory.recalcular(tablaRef[0].getItems(), actualizarCampos); // ← corrección clave
            tablaRef[0].refresh();
            recalcularTotales = () -> {
                TablaProduccionesFactory.recalcular(tablaRef[0].getItems(), actualizarCampos);
                tablaRef[0].refresh();
            };
        }

        contenedor.getChildren().addAll(tabla, resumen);
        return contenedor;
    }
}
