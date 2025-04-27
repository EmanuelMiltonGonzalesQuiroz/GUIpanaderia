package com.panaderiafx.controllers.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.BiConsumer;

public class CampoSeleccionExtendido extends VBox {

    private final TextField campoPersonalizado = new TextField();
    private final Label seleccionLabel = new Label("Nada seleccionado");
    private final VBox tablaContenedor = new VBox(); // ahora sí se usa

    private final String tablaOrigen;
    private final List<String> columnasMostrar;
    private final String columnaCargar;

    private BiConsumer<String, CampoSeleccionExtendido> onSeleccionarListener;

    public CampoSeleccionExtendido(String tablaOrigen, String columnasMostrarTexto, String columnaCargar, String valorInicial) {
        this.tablaOrigen = tablaOrigen;
        this.columnaCargar = columnaCargar;
        this.columnasMostrar = Arrays.stream(columnasMostrarTexto.split(","))
                                     .map(String::trim)
                                     .filter(s -> !s.isEmpty())
                                     .toList();

        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.TOP_LEFT);

        Button botonSeleccionar = new Button("Seleccionar");
        botonSeleccionar.setOnAction(e -> {
            if (onSeleccionarListener != null) {
                onSeleccionarListener.accept(String.join(",", columnasMostrar), this);
            }
        });

        campoPersonalizado.setPromptText("O escriba su propia opción");
        campoPersonalizado.setPrefWidth(200);
        campoPersonalizado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                actualizarLabel(newVal);
            } else {
                seleccionLabel.setText("Nada seleccionado");
            }
        });

        if (!valorInicial.isBlank()) {
            campoPersonalizado.setText(valorInicial);
            actualizarLabel(valorInicial);
        }

        HBox filaSuperior = new HBox(10, botonSeleccionar, campoPersonalizado);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        seleccionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");

        tablaContenedor.setVisible(false);
        tablaContenedor.setManaged(false);

        this.getChildren().addAll(filaSuperior, seleccionLabel);
    }

    private void actualizarLabel(String valor) {
        if (!columnasMostrar.isEmpty()) {
            String bonito = columnasMostrar.get(0).substring(0, 1).toUpperCase() + columnasMostrar.get(0).substring(1);
            seleccionLabel.setText(bonito + " seleccionado: " + valor);
        } else {
            seleccionLabel.setText("Seleccionado: " + valor);
        }
    }

    public void setValorDesdeTabla(String valor) {
        campoPersonalizado.setText(valor);
        actualizarLabel(valor);
    }

    public String getValorSeleccionado() {
        return campoPersonalizado.getText().trim();
    }

    public void setOnSeleccionarListener(BiConsumer<String, CampoSeleccionExtendido> listener) {
        this.onSeleccionarListener = listener;
    }

    public VBox getContenedorTabla() {
        return tablaContenedor;
    }

    public String getTabla() {
        return tablaOrigen;
    }

    public String getColumna() {
        return columnaCargar;
    }
}
