package com.panaderiafx.controllers.components;

import javafx.beans.property.SimpleStringProperty;
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

    private double ancho = -1;
    private double alto = -1;

    private final Label infoPagina = new Label();

    public TablaInteractiva(List<Map<String, String>> datos, List<String> columnas, int filasPorPagina) {
        this.datosOriginales = new ArrayList<>(datos);
        this.datosTotales = FXCollections.observableArrayList(datos);
        this.columnas = columnas;
        this.filasPorPagina = filasPorPagina;

        this.setStyle("-fx-background-color: #FFF3E0;");

        determinarColumnasVisibles(datos);
        construirColumnas();

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-font-size: 14px;");
        tabla.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Map<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #FFFFFF;");
                } else {
                    setStyle("-fx-background-color: #FFF8E1;");
                }
            }
        });

        VBox contenedorCentral = new VBox(10);
        contenedorCentral.setPadding(new Insets(10));
        contenedorCentral.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        contenedorCentral.getChildren().addAll(tabla, crearControlesPaginacion());

        this.setTop(crearCabeceraBusquedaYPaginado());
        this.setCenter(contenedorCentral);
        configurarPaginacion(0);
    }

    private void determinarColumnasVisibles(List<Map<String, String>> datos) {
        if (columnas == null || columnas.isEmpty()) {
            columnasVisibles = datos.isEmpty() ? new ArrayList<>() : new ArrayList<>(datos.get(0).keySet());
        } else {
            columnasVisibles = columnas;
        }
    }

    private void construirColumnas() {
        tabla.getColumns().clear();
        for (String columna : columnasVisibles) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columna);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(columna, "")));
            col.setSortable(true);
            tabla.getColumns().add(col);
        }
    }

    private HBox crearControlesPaginacion() {
        Button anterior = new Button("⬅ Anterior");
        Button siguiente = new Button("Siguiente ➡");

        anterior.setOnAction(e -> cambiarPagina(-1));
        siguiente.setOnAction(e -> cambiarPagina(1));

        HBox paginador = new HBox(15, anterior, infoPagina, siguiente);
        paginador.setAlignment(Pos.CENTER);
        return paginador;
    }

    private int paginaActual = 0;

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
            return;
        }

        int desde = pagina * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, datosTotales.size());
        tabla.setItems(FXCollections.observableArrayList(datosTotales.subList(desde, hasta)));

        int totalPaginas = (int) Math.ceil((double) datosTotales.size() / filasPorPagina);
        infoPagina.setText((pagina + 1) + "/" + totalPaginas);
    }

    private VBox crearCabeceraBusquedaYPaginado() {
        Label titulo = new Label("Buscar registros o ajustar visualización");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-background-color: #FFF9C4;" +
                "-fx-padding: 10px;" +
                "-fx-border-radius: 5px;" +
                "-fx-background-radius: 5px;" +
                "-fx-border-color: #FBC02D;" +
                "-fx-border-width: 1.5px;"
        );
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                datosTotales = FXCollections.observableArrayList(datosOriginales);
            } else {
                String buscar = newVal.toLowerCase();
                List<Map<String, String>> filtrados = datosOriginales.stream()
                        .filter(map -> map.values().stream()
                                .anyMatch(val -> val.toLowerCase().contains(buscar)))
                        .collect(Collectors.toList());
                datosTotales = FXCollections.observableArrayList(filtrados);
            }
            configurarPaginacion(0);
        });

        ComboBox<String> selectorFilas = new ComboBox<>();
        selectorFilas.getItems().addAll("Todos", "20", "50", "100");
        selectorFilas.setValue(filasPorPagina == -1 ? "Todos" : String.valueOf(filasPorPagina));
        selectorFilas.setStyle("-fx-font-size: 14px;");
        selectorFilas.setOnAction(e -> {
            String seleccion = selectorFilas.getValue();
            if ("Todos".equals(seleccion)) {
                filasPorPagina = -1;
            } else {
                filasPorPagina = Integer.parseInt(seleccion);
            }
            configurarPaginacion(0);
        });

        HBox filtro = new HBox(10, campoBusqueda, selectorFilas);
        filtro.setAlignment(Pos.CENTER_LEFT);
        filtro.setPadding(new Insets(5, 0, 0, 0));

        VBox contenedor = new VBox(5, titulo, filtro);
        contenedor.setPadding(new Insets(15, 15, 5, 15));
        return contenedor;
    }

    public void actualizarDatos(List<Map<String, String>> nuevosDatos) {
        this.datosOriginales = new ArrayList<>(nuevosDatos);
        this.datosTotales = FXCollections.observableArrayList(nuevosDatos);
        determinarColumnasVisibles(nuevosDatos);
        construirColumnas();
        configurarPaginacion(0);
    }

    public TableView<Map<String, String>> getTabla() {
        return tabla;
    }

    public void setAncho(double ancho) {
        this.ancho = ancho;
        this.setPrefWidth(ancho);
        this.setMaxWidth(ancho);
    }

    public void setAlto(double alto) {
        this.alto = alto;
        this.setPrefHeight(alto);
        this.setMaxHeight(alto);
    }
}
