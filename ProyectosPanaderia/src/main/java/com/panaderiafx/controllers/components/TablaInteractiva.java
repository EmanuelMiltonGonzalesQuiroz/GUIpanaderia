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
    private final Pagination paginador = new Pagination();
    private int filasPorPagina;
    private List<String> columnas;
    private List<String> columnasVisibles;

    private double ancho = -1;
    private double alto = -1;

    public TablaInteractiva(List<Map<String, String>> datos, List<String> columnas, int filasPorPagina) {
        this.datosOriginales = new ArrayList<>(datos); // ⬅ Guardar copia original
        this.datosTotales = FXCollections.observableArrayList(datos);
        this.columnas = columnas;
        this.filasPorPagina = filasPorPagina;

        this.setStyle("-fx-background-color: #FFF3E0;");
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        determinarColumnasVisibles(datos);
        construirColumnas();
        configurarPaginacion();

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

        this.setTop(crearCabeceraBusquedaYPaginado());
    }

    private void determinarColumnasVisibles(List<Map<String, String>> datos) {
        if (columnas == null || columnas.isEmpty()) {
            columnasVisibles = datos.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(datos.get(0).keySet());
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

    private VBox crearPagina(int paginaIndice) {
        int desde = paginaIndice * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, datosTotales.size());

        tabla.setItems(FXCollections.observableArrayList(
            datosTotales.subList(desde, hasta)
        ));

        VBox contenedor = new VBox(tabla);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setFillWidth(true);
        contenedor.setPadding(new Insets(10));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        contenedor.setPrefWidth(ancho > 0 ? ancho : Double.MAX_VALUE);
        contenedor.setPrefHeight(alto > 0 ? alto : Double.MAX_VALUE);

        return contenedor;
    }

    private void configurarPaginacion() {
        if (filasPorPagina == -1) {
            tabla.setItems(datosTotales);
            this.setCenter(tabla);
            return;
        }

        int totalPaginas = (int) Math.ceil((double) datosTotales.size() / filasPorPagina);
        paginador.setPageCount(Math.max(1, totalPaginas));
        paginador.setPageFactory(this::crearPagina);
        this.setCenter(paginador);
    }

    private HBox crearCabeceraBusquedaYPaginado() {
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
            configurarPaginacion();
        });

        ComboBox<String> selectorFilas = new ComboBox<>();
        selectorFilas.getItems().addAll("Todos", "20", "50", "100");
        selectorFilas.setValue("Todos");
        selectorFilas.setStyle("-fx-font-size: 14px;");
        selectorFilas.setOnAction(e -> {
            String seleccion = selectorFilas.getValue();
            if ("Todos".equals(seleccion)) {
                filasPorPagina = -1;
            } else {
                filasPorPagina = Integer.parseInt(seleccion);
            }
            configurarPaginacion();
        });

        HBox contenedor = new HBox(10, campoBusqueda, selectorFilas);
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setPadding(new Insets(10, 10, 0, 10));
        contenedor.setStyle("-fx-background-color: transparent;");
        return contenedor;
    }

    public void actualizarDatos(List<Map<String, String>> nuevosDatos) {
        this.datosOriginales = new ArrayList<>(nuevosDatos);
        this.datosTotales = FXCollections.observableArrayList(nuevosDatos);
        determinarColumnasVisibles(nuevosDatos);
        construirColumnas();
        configurarPaginacion();
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
