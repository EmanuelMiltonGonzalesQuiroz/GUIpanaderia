package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.cache.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public class PanelResumenProduccion extends VBox {
    private final TextField gananciaField = new TextField();
    private final TextField costoDirectoField = new TextField();
    private final TextField costoIndirectoField = new TextField();
    private final TextField parametrosField = new TextField();
    private final TextField totalField = new TextField();

    private double ganancia;
    private double costoDirecto;
    private double costoIndirecto;
    private double parametros;

    public PanelResumenProduccion(double ganancia, double costoDirecto, double costoIndirecto, double parametros, double total,
                                  Consumer<String> onClick) {
        this.ganancia = ganancia;
        this.costoDirecto = costoDirecto;
        this.costoIndirecto = costoIndirecto;
        this.parametros = parametros;

        setSpacing(10);
        setStyle("-fx-background-color: #F36C00; -fx-padding: 20; -fx-background-radius: 10;");

        getChildren().addAll(
            fila("GANANCIA B.", gananciaField, ganancia, onClick),
            fila("COSTOS DIRECTOS R.", costoDirectoField, costoDirecto, onClick),
            fila("COSTOS INDIRECTOS", costoIndirectoField, costoIndirecto, onClick),
            fila("PARÁMETROS", parametrosField, parametros, onClick),
            fila("TOTAL", totalField, total, null)
        );

        // Observadores conectados a los 4 caches
        CacheGananciasUtils.agregarObservador(this::actualizarGanancia);
        CacheCostosDirectosUtils.agregarObservador(this::actualizarCostoDirecto);
        CacheCostosIndirectosUtils.agregarObservador(this::actualizarCostoIndirecto);
        CacheParametrosUtils.agregarObservador(this::actualizarParametros);

        // Inicialización visual
        actualizarTodo();
    }

    private HBox fila(String titulo, TextField campo, double valor, Consumer<String> onClick) {
        Label etiqueta = new Label(titulo);
        etiqueta.setMinWidth(150);
        etiqueta.setStyle("-fx-font-weight: bold; -fx-background-color: #FFC107; -fx-padding: 5 10 5 10;");

        campo.setText(String.format("%.2f", valor));
        campo.setEditable(false);
        campo.setStyle("-fx-font-weight: bold;");
        campo.setMinWidth(100);

        HBox fila = new HBox(10, etiqueta, campo);
        if (onClick != null) {
            fila.setOnMouseClicked(e -> onClick.accept(titulo.replace(" ", "_")));
        }
        return fila;
    }

    private void actualizarGanancia() {
        this.ganancia = CacheGananciasUtils.get();
        gananciaField.setText(String.format("%.2f", ganancia));
        actualizarTotal();
    }

    private void actualizarCostoDirecto() {
        this.costoDirecto = CacheCostosDirectosUtils.get();
        costoDirectoField.setText(String.format("%.2f", costoDirecto));
        actualizarTotal();
    }

    private void actualizarCostoIndirecto() {
        this.costoIndirecto = CacheCostosIndirectosUtils.get();
        costoIndirectoField.setText(String.format("%.2f", costoIndirecto));
        actualizarTotal();
    }

    private void actualizarParametros() {
        this.parametros = CacheParametrosUtils.get();
        parametrosField.setText(String.format("%.2f", parametros));
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = ganancia - costoDirecto - costoIndirecto - parametros;
        totalField.setText(String.format("%.2f", total));
    }

    public void actualizarGananciaYCosto(double nuevaGanancia, double nuevoCostoDirecto) {
        this.ganancia = nuevaGanancia;
        this.costoDirecto = nuevoCostoDirecto;
        gananciaField.setText(String.format("%.2f", ganancia));
        costoDirectoField.setText(String.format("%.2f", costoDirecto));
        actualizarTotal();
    }

    private void actualizarTodo() {
        actualizarGanancia();
        actualizarCostoDirecto();
        actualizarCostoIndirecto();
        actualizarParametros();
    }
    public void actualizarParametros(double nuevoValor) {
        this.parametros = nuevoValor;
        parametrosField.setText(String.format("%.2f", nuevoValor));
        actualizarTotal();
    }
    
    
}
