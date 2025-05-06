package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VistaGananciasCostosDirectos {

    public static Node crear(String fecha, String tipo, Consumer<String> accionEditar, BiConsumer<Double, Double> actualizarTotales) {
        Label ganLab = new Label("TOTAL GANANCIAS");
        TextField ganField = new TextField("0.00");
        ganField.setEditable(false);

        Label cosLab = new Label("TOTAL COSTOS");
        TextField cosField = new TextField("0.00");
        cosField.setEditable(false);

        HBox resumen = new HBox(10, ganLab, ganField, cosLab, cosField);
        resumen.setPadding(new Insets(10));
        resumen.setStyle("-fx-background-color: #FFA726;");

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(15));
        contenedor.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");
        contenedor.setFillWidth(true);

        Label titulo = new Label("GANANCIAS Y COSTOS DIRECTOS");
        titulo.setStyle("-fx-font-weight: bold;");

        contenedor.getChildren().addAll(titulo);

        contenedor.getChildren().add(
            TablaProduccionesFactory.crearTabla(
                fecha, tipo,
                accionEditar,
                (ganancia, costo) -> {
                    ganField.setText(String.format("%.2f", ganancia));
                    cosField.setText(String.format("%.2f", costo));
                    actualizarTotales.accept(ganancia, costo);
                }
            )
        );

        contenedor.getChildren().add(resumen);
        return contenedor;
    }
}
