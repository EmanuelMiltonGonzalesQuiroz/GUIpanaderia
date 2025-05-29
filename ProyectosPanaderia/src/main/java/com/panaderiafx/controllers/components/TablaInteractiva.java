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
import java.util.stream.Collectors;

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

        List<Map<String, String>> datosTransformados = datos;
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
        VBox.setVgrow(contenedorCentral, Priority.ALWAYS);
        contenedorCentral.getChildren().addAll(tabla, paginacion);

        // CABECERA BUSQUEDA + SELECTOR FUENTE
        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle("-fx-font-size: 14px; -fx-background-color: #FFF9C4; -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #FBC02D; -fx-border-width: 1.5px;");
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        ComboBox<String> selectorFilas = new ComboBox<>();
        selectorFilas.getItems().addAll("Todos", "20", "50", "100");
        selectorFilas.setValue("20");
        selectorFilas.setStyle("-fx-font-size: 16px;");
        selectorFilas.setOnAction(e -> {
            String seleccion = selectorFilas.getValue();
            if ("Todos".equals(seleccion)) {
                cambiarFilasPorPagina(-1);
            } else {
                cambiarFilasPorPagina(Integer.parseInt(seleccion));
            }
        });

        BotonActualizar botonActualizar = new BotonActualizar(this::recargarDesdeExcel);

        SelectorTamanoFuente selectorFuente = new SelectorTamanoFuente(this::aplicarTamanoFuente);

        HBox filtro = new HBox(10, campoBusqueda, selectorFilas, botonActualizar, selectorFuente);
        filtro.setAlignment(Pos.CENTER_LEFT);
        filtro.setPadding(new Insets(5, 15, 0, 15));

        Label titulo = new Label("Buscar registros o ajustar visualización");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox cabecera = new VBox(10, titulo, filtro);
        cabecera.setPadding(new Insets(15, 15, 5, 15));

        // BUSQUEDA LÓGICA
        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> actualizarFiltro(newVal));

        VBox.setVgrow(this, Priority.ALWAYS);
        this.setTop(cabecera);
        this.setCenter(contenedorCentral);
        configurarPaginacion(0);
    }

    private void aplicarTamanoFuente(int sizePx) {
        String estilo = "-fx-font-size: " + sizePx + "px;";
        tabla.setStyle(estilo);
        this.lookupAll(".text-field").forEach(n -> n.setStyle(estilo));
        this.lookupAll(".combo-box").forEach(n -> n.setStyle(estilo));
        this.lookupAll(".button").forEach(n -> n.setStyle(estilo));
        this.lookupAll(".label").forEach(n -> n.setStyle(estilo));
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
        if (texto == null || texto.trim().isEmpty()) {
            datosTotales = FXCollections.observableArrayList(datosOriginales);
        } else {
            // Separar por coma y limpiar términos vacíos
            String[] terminos = texto.split(",");
            String patron = Arrays.stream(terminos)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("|"));

            if (!patron.isEmpty()) {
                // Usa las columnas visibles realmente presentes en la tabla
                datosTotales = TablaUtils.filtrarConColumnas(datosOriginales, texto, columnasVisibles);
            } else {
                datosTotales = FXCollections.observableArrayList(datosOriginales);
            }
        }
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
