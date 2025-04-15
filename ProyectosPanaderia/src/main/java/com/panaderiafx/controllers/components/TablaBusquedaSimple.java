package com.panaderiafx.controllers.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TablaBusquedaSimple extends VBox {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final List<Map<String, String>> datosOriginales;
    private final ObservableList<Map<String, String>> datosVisibles;
    private final String columna;
    private Consumer<String> onSeleccionar;

    public TablaBusquedaSimple(List<Map<String, String>> datos, String columna) {
        this.columna = columna;
        this.datosOriginales = new ArrayList<>(datos);
        this.datosVisibles = FXCollections.observableArrayList(datos);

        this.setPadding(new Insets(10));
        this.setSpacing(10);
        this.setStyle("-fx-background-color: #FFFDE7;");

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

        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> filtrar(newVal));
        construirTabla();

        this.getChildren().addAll(campoBusqueda, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
    }

    private void construirTabla() {
        tabla.getColumns().clear();
        TableColumn<Map<String, String>, String> col = new TableColumn<>(columna);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(columna, "")));
        tabla.getColumns().add(col);

        tabla.setItems(datosVisibles);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        tabla.setRowFactory(tv -> {
            TableRow<Map<String, String>> row = new TableRow<>();
            row.setOnMouseClicked((MouseEvent e) -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    String valor = row.getItem().getOrDefault(columna, "");
                    System.out.println("🟢 Doble clic en tabla: " + valor);
                    if (onSeleccionar != null && !valor.isBlank()) {
                        onSeleccionar.accept(valor);
                    }
                }
            });
            return row;
        });
    }

    private void filtrar(String texto) {
        if (texto == null || texto.isBlank()) {
            datosVisibles.setAll(datosOriginales);
        } else {
            String buscar = texto.toLowerCase();
            List<Map<String, String>> filtrados = datosOriginales.stream()
                .filter(m -> m.getOrDefault(columna, "").toLowerCase().contains(buscar))
                .collect(Collectors.toList());
            datosVisibles.setAll(filtrados);
        }
    }

    public void setOnSeleccionar(Consumer<String> listener) {
        this.onSeleccionar = listener;
    }

    public Node getNode() {
        return this;
    }
}
