package com.panaderiafx.controllers.components;

import com.panaderiafx.controllers.components.table.*;
import com.panaderiafx.utils.RelacionadorVisual;
import com.panaderiafx.utils.VerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class TablaInteractiva extends BorderPane {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private List<Map<String, String>> datosOriginales;
    private ObservableList<Map<String, String>> datosTotales;
    private int filasPorPagina;
    private List<String> columnas;
    private List<String> columnasVisibles;

    private int paginaActual = 0;
    private final Label infoPagina = new Label();
    private final ControlPaginacion paginacion;
    private final String nombreTabla;

    public TablaInteractiva(List<Map<String, String>> datos, List<String> columnas, int filasPorPagina, String nombreTabla) {
        this.nombreTabla = nombreTabla;

        List<Map<String, String>> datosTransformados = RelacionadorVisual.aplicarSustituciones(nombreTabla, datos);
        this.datosOriginales = new ArrayList<>(datosTransformados);
        this.datosTotales = FXCollections.observableArrayList(datosTransformados);
        this.columnas = columnas;
        this.filasPorPagina = filasPorPagina;

        this.setStyle("-fx-background-color: #FFF3E0;");

        columnasVisibles = TablaUtils.determinarColumnasVisibles(datosTransformados, columnas);
        TablaFactory.construirColumnas(tabla, columnasVisibles);
        TablaUtils.configurarEstiloFilas(tabla);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-font-size: 14px;");
        tabla.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        paginacion = new ControlPaginacion(this::cambiarPagina, infoPagina);

        VBox contenedorCentral = new VBox(10);
        contenedorCentral.setPadding(new Insets(10));
        contenedorCentral.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        contenedorCentral.getChildren().addAll(tabla, paginacion);

        CabeceraBusqueda cabecera = new CabeceraBusqueda(
                datosOriginales,
                this::actualizarFiltro,
                this::cambiarFilasPorPagina,
                this::recargarDesdeExcel
        );

        this.setTop(cabecera);
        this.setCenter(contenedorCentral);
        configurarPaginacion(0);
    }

    private void cambiarPagina(int cambio) {
        int totalPaginas = (int) Math.ceil((double) datosTotales.size() / filasPorPagina);
        int nuevaPagina = Math.max(0, Math.min(paginaActual + cambio, totalPaginas - 1));
        configurarPaginacion(nuevaPagina);
    }

    private void configurarPaginacion(int pagina) {
        this.paginaActual = pagina;

        if (filasPorPagina == -1) {
            tabla.setItems(datosTotales);
            infoPagina.setText("Mostrando todos los registros");
            paginacion.mostrarBotones(false);
            return;
        }

        paginacion.mostrarBotones(true);
        int desde = pagina * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, datosTotales.size());

        if (desde >= datosTotales.size()) {
            desde = 0;
        }
        if (hasta > datosTotales.size()) {
            hasta = datosTotales.size();
        }

        ObservableList<Map<String, String>> paginaDatos = FXCollections.observableArrayList(datosTotales.subList(desde, hasta));
        tabla.setItems(paginaDatos);

        int totalPaginas = (int) Math.ceil((double) datosTotales.size() / filasPorPagina);
        infoPagina.setText((pagina + 1) + "/" + totalPaginas);
    }

    private void actualizarFiltro(String texto) {
        datosTotales = TablaUtils.filtrar(datosOriginales, texto);
        configurarPaginacion(0);
    }

    private void cambiarFilasPorPagina(int filas) {
        this.filasPorPagina = filas;
        configurarPaginacion(0);
    }

    private void recargarDesdeExcel() {
        List<Map<String, String>> nuevosDatos = VerUtils.verTabla(nombreTabla);
        actualizarDatos(nuevosDatos);
    }

    public void actualizarDatos(List<Map<String, String>> nuevosDatos) {
        List<Map<String, String>> datosTransformados = RelacionadorVisual.aplicarSustituciones(nombreTabla, nuevosDatos);
        this.datosOriginales = new ArrayList<>(datosTransformados);
        this.datosTotales = FXCollections.observableArrayList(datosTransformados);
        columnasVisibles = TablaUtils.determinarColumnasVisibles(datosTransformados, columnas);

        if (!tabla.getColumns().isEmpty()) {
            tabla.getColumns().clear();
        }
        TablaFactory.construirColumnas(tabla, columnasVisibles);
        configurarPaginacion(0);
    }

    public TableView<Map<String, String>> getTabla() {
        return tabla;
    }

    public void setAncho(double ancho) {
        this.setPrefWidth(ancho);
        this.setMaxWidth(ancho);
    }

    public void setAlto(double alto) {
        this.setPrefHeight(alto);
        this.setMaxHeight(alto);
    }
}
