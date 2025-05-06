package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public class PanelResumenProduccion extends VBox {
    private final TextField gananciaField = new TextField();
    private final TextField costoDirectoField = new TextField();
    private final TextField costoIndirectoField = new TextField();
    private final TextField parametrosField = new TextField();
    private final TextField totalField = new TextField();

    private double ganancia, costoDirecto, costoIndirecto, parametros;

    public PanelResumenProduccion(double ganancia, double costoDirecto, double costoIndirecto, double parametros, double total,
                                  Consumer<String> onClick) {
        this.ganancia = ganancia;
        this.costoDirecto = costoDirecto;
        this.costoIndirecto = costoIndirecto;
        this.parametros = parametros;

        setSpacing(10);
        setStyle("-fx-background-color: #F36C00; -fx-padding: 20; -fx-background-radius: 10;");

        getChildren().addAll(
            fila("GANANCIAS", gananciaField, ganancia, onClick),
            fila("COSTOS DIRECTOS", costoDirectoField, costoDirecto, onClick),
            fila("COSTOS INDIRECTOS", costoIndirectoField, costoIndirecto, null),
            fila("PARÁMETROS", parametrosField, parametros, null),
            fila("TOTAL", totalField, total, null)
        );
    }

    private HBox fila(String titulo, TextField campo, double valor, Consumer<String> onClick) {
        Label etiqueta = new Label(titulo);
        etiqueta.setMinWidth(150);
        etiqueta.setStyle("-fx-font-weight: bold; -fx-background-color: #FFC107; -fx-padding: 5 10 5 10;");

        campo.setText(String.format("%.2f", valor));
        campo.setEditable(false);
        campo.setStyle("-fx-font-weight: bold;");

        HBox fila = new HBox(10, etiqueta, campo);
        if (onClick != null) {
            fila.setOnMouseClicked(e -> onClick.accept(titulo.replace(" ", "_")));
        }
        return fila;
    }

    public void actualizarGananciaYCosto(double nuevaGanancia, double nuevoCostoDirecto) {
        this.ganancia = nuevaGanancia;
        this.costoDirecto = nuevoCostoDirecto;
        gananciaField.setText(String.format("%.2f", ganancia));
        costoDirectoField.setText(String.format("%.2f", costoDirecto));
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = ganancia - costoDirecto - costoIndirecto - parametros;
        totalField.setText(String.format("%.2f", total));
    }
}
