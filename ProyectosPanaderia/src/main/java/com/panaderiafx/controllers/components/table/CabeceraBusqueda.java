package com.panaderiafx.controllers.components.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CabeceraBusqueda extends VBox {

    private final TextField campoBusqueda = new TextField();
    private final ComboBox<String> selectorFilas = new ComboBox<>();
    private ObservableList<Map<String, String>> datosOriginales;

    public CabeceraBusqueda(List<Map<String, String>> datosOriginales,
                            Consumer<String> onBuscar,
                            Consumer<Integer> onSeleccionFilas,
                            Runnable accionActualizar) {

        this.datosOriginales = FXCollections.observableArrayList(datosOriginales);

        Label titulo = new Label("Buscar registros o ajustar visualización");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        campoBusqueda.setPromptText("\uD83D\uDD0D Buscar...");
        campoBusqueda.setStyle("""
                -fx-font-size: 16px;
                -fx-background-color: #FFF9C4;
                -fx-padding: 10px;
                -fx-border-radius: 5px;
                -fx-background-radius: 5px;
                -fx-border-color: #FBC02D;
                -fx-border-width: 1.5px;
        """);
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> onBuscar.accept(newVal));

        selectorFilas.getItems().addAll("Todos", "20", "50", "100");
        selectorFilas.setValue("20");
        selectorFilas.setStyle("-fx-font-size: 14px;");
        selectorFilas.setOnAction(e -> {
            String seleccion = selectorFilas.getValue();
            if ("Todos".equals(seleccion)) {
                onSeleccionFilas.accept(-1);
            } else {
                onSeleccionFilas.accept(Integer.parseInt(seleccion));
            }
        });

        HBox filtro = new HBox(10, campoBusqueda, selectorFilas);
        filtro.setAlignment(Pos.CENTER_LEFT);
        filtro.setPadding(new Insets(5, 0, 0, 0));

        if (accionActualizar != null) {
            filtro.getChildren().add(new BotonActualizar(accionActualizar));
        }

        this.getChildren().addAll(titulo, filtro);
        this.setPadding(new Insets(15, 15, 5, 15));
    }

    public void setDatosOriginales(List<Map<String, String>> nuevosDatos) {
        this.datosOriginales = FXCollections.observableArrayList(nuevosDatos);
    }

    public CabeceraBusqueda(List<Map<String, String>> datosOriginales,
                            Consumer<String> onBuscar,
                            Consumer<Integer> onSeleccionFilas) {
        this(datosOriginales, onBuscar, onSeleccionFilas, null);
    }
}