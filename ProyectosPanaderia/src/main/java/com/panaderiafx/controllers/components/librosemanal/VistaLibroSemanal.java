package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;

public class VistaLibroSemanal extends VBox {

    private final DetallePorDiaVista detalle = new DetallePorDiaVista();
    private final ScrollPane panelDetalle = new ScrollPane();
    private final ResumenSemanal resumen = new ResumenSemanal();

    public VistaLibroSemanal() {
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #FFF3E0;");

        Label titulo = new Label("📘 Libro Semanal de Flujo de Caja");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        SelectorSemana selector = new SelectorSemana();

        Button botonActualizar = new Button("🔄 ACTUALIZAR");
        botonActualizar.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12;");
        botonActualizar.setOnAction(e -> actualizarTodo(selector.getFechaInicioSemana()));

        HBox filaAcciones = new HBox(10, selector, botonActualizar);
        filaAcciones.setPadding(new Insets(5));
        filaAcciones.setStyle("-fx-background-color: #FFB74D; -fx-padding: 10;");

        HBox cuerpo = new HBox(30);
        cuerpo.setPadding(new Insets(10));

        panelDetalle.setFitToWidth(true);
        panelDetalle.setPrefWidth(1000);
        panelDetalle.setStyle("-fx-background: #FFF3E0;");
        panelDetalle.setVisible(false);

        resumen.setOnMostrarDetalle(tipo -> {
            panelDetalle.setVisible(true);
            actualizarDetalleDesdeResumen(selector.getFechaInicioSemana(), tipo);
        });

        cuerpo.getChildren().addAll(resumen, panelDetalle);

        selector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) actualizarTodo(newVal);
        });

        LocalDate fechaInicial = selector.getFechaInicioSemana();
        actualizarTodo(fechaInicial);

        getChildren().addAll(titulo, filaAcciones, cuerpo);
    }

    private void actualizarTodo(LocalDate fecha) {
        resumen.setFechaInicio(fecha); // ✅ Esta línea es esencial
        LibroSemanalService.cargarResumenSemanal(fecha);
        panelDetalle.setVisible(false);
    }
    

    private void actualizarDetalleDesdeResumen(LocalDate fecha, Tipo tipo) {
        if (tipo == Tipo.GANANCIA_B || tipo == Tipo.COSTO_DIRECTO) {
            detalle.actualizarSemana(fecha);
            LibroSemanalService.cargarDetallePorDia(fecha, detalle);
            panelDetalle.setContent(detalle);
        } else if (tipo == Tipo.COSTO_INDIRECTO) {
            Node panelCostos = PanelDetalleCostosIndirectos.crear();
            panelDetalle.setContent(panelCostos);
        } else if (tipo == Tipo.PARAMETROS) {
            Node panelCostos = PanelDetalleParametros.crear();
            panelDetalle.setContent(panelCostos);
        } else if (tipo == Tipo.COSTOS_DIA) {
            PanelDetalleVariableDia.setFechaInicio(fecha);
            Node panelCostos = PanelDetalleVariableDia.crear();
            panelDetalle.setContent(panelCostos);
        }
        
    }
}
