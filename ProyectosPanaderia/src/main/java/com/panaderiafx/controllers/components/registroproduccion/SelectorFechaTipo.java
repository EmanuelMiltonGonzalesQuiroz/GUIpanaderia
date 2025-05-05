package com.panaderiafx.controllers.components.registroproduccion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SelectorFechaTipo extends VBox {

    private final DatePicker selectorFecha;
    private final ComboBox<String> selectorTipo;
    private final Button botonSeleccionar;
    private final Button botonActualizar;

    public SelectorFechaTipo() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");

        HBox filaFecha = new HBox(10);
        filaFecha.setAlignment(Pos.CENTER_LEFT);
        Label lblFecha = new Label("Fecha:");
        lblFecha.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        selectorFecha = new DatePicker(LocalDate.now());
        botonSeleccionar = new Button("SELECCIONAR");
        estilizarBoton(botonSeleccionar);
        filaFecha.getChildren().addAll(lblFecha, selectorFecha, botonSeleccionar);

        HBox filaTipo = new HBox(10);
        filaTipo.setAlignment(Pos.CENTER_LEFT);
        Label lblTipo = new Label("TIPO");
        lblTipo.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        selectorTipo = new ComboBox<>();
        selectorTipo.getItems().addAll("DÍA", "MES");
        selectorTipo.setValue("DÍA");
        botonActualizar = new Button("ACTUALIZAR");
        estilizarBoton(botonActualizar);
        filaTipo.getChildren().addAll(lblTipo, selectorTipo, botonActualizar);

        this.getChildren().addAll(filaFecha, filaTipo);
    }

    private void estilizarBoton(Button b) {
        b.setStyle("-fx-background-color: #FFB74D; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    }

    public String getFecha() {
        LocalDate fecha = selectorFecha.getValue();
        return selectorTipo.getValue().equalsIgnoreCase("DÍA")
                ? fecha.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
                : fecha.format(DateTimeFormatter.ofPattern("M/yyyy"));
    }

    public String getTipo() {
        return selectorTipo.getValue();
    }

    public Button getBotonActualizar() {
        return botonActualizar;
    }

    public Button getBotonSeleccionar() {
        return botonSeleccionar;
    }
}
