package com.panaderiafx.controllers.components.forms;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.stream.Collectors;

public class TablaBusquedaRegistros {

    public static Node crear(List<Map<String, String>> registros, int columnasMaximas) {
        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tabla.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        List<String> columnasTotales = new ArrayList<>(registros.get(0).keySet());
        int columnasDisponibles = columnasTotales.size();

        Spinner<Integer> selectorColumnas = new Spinner<>(1, columnasDisponibles, 2);
        selectorColumnas.setEditable(false);
        selectorColumnas.setPrefWidth(80);

        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: #FFF9C4;" +
            "-fx-padding: 8px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #FBC02D;" +
            "-fx-border-width: 1.5px;"
        );

        Label labelColumnas = new Label("Columnas:");
        HBox selectorBox = new HBox(10, labelColumnas, selectorColumnas);
        selectorBox.setStyle("-fx-alignment: center-left;");

        Button botonRecargar = new Button("🔁 Recargar");
        botonRecargar.setStyle(
            "-fx-background-color: #FF9800;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 12;" +
            "-fx-background-radius: 5px;"
        );
        botonRecargar.setOnAction(e -> {
            campoBusqueda.clear();
            selectorColumnas.getValueFactory().setValue(2);
        });

        HBox filaSuperior = new HBox(20, botonRecargar, campoBusqueda);
        filaSuperior.setStyle("-fx-alignment: center-left;");
        filaSuperior.setFillHeight(true);

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setFitToHeight(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollTabla, Priority.ALWAYS);

        VBox contenedor = new VBox(10, filaSuperior, selectorBox, scrollTabla);
        VBox.setVgrow(contenedor, Priority.ALWAYS);
        contenedor.setUserData(tabla);

        List<String> columnasVisibles = new ArrayList<>();

        Runnable actualizarColumnas = () -> {
            int cantidad = selectorColumnas.getValue();
            columnasVisibles.clear();
            columnasVisibles.addAll(columnasTotales.subList(0, Math.min(cantidad, columnasTotales.size())));

            tabla.getColumns().clear();
            for (String col : columnasVisibles) {
                TableColumn<Map<String, String>, String> columna = new TableColumn<>(col);
                columna.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(col, "")));
                tabla.getColumns().add(columna);
            }

            aplicarFiltro(campoBusqueda.getText(), tabla, registros, columnasVisibles);
        };

        campoBusqueda.textProperty().addListener((obs, ov, nv) -> aplicarFiltro(nv, tabla, registros, columnasVisibles));
        selectorColumnas.valueProperty().addListener((obs, ov, nv) -> actualizarColumnas.run());

        selectorColumnas.getValueFactory().setValue(2);
        actualizarColumnas.run();

        return contenedor;
    }

    private static void aplicarFiltro(String texto, TableView<Map<String, String>> tabla,
                                      List<Map<String, String>> registros, List<String> columnasVisibles) {
        String filtro = texto.toLowerCase();
        List<Map<String, String>> filtrados = registros.stream()
            .filter(fila -> columnasVisibles.stream()
                .anyMatch(col -> fila.getOrDefault(col, "").toLowerCase().contains(filtro)))
            .collect(Collectors.toList());

        tabla.getItems().clear();
        tabla.getItems().addAll(filtrados);
    }
}
