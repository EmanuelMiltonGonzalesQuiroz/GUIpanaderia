package com.panaderiafx.controllers.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.BiConsumer;
public class CampoSeleccionExtendido extends VBox {

    private final TextField campoPersonalizado = new TextField();
    private final Label seleccionLabel = new Label("Nada seleccionado");
    private final VBox tablaContenedor = new VBox();
    private final String tabla;
    private final String columna;
    private BiConsumer<String, CampoSeleccionExtendido> onSeleccionarListener;

    public CampoSeleccionExtendido(String tabla, String columna) {
        this(tabla, columna, ""); // delega al constructor principal
    }

    public CampoSeleccionExtendido(String tabla, String columna, String valorInicial) {
        this.tabla = tabla;
        this.columna = columna;

        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.TOP_LEFT);

        Button botonSeleccionar = new Button("Seleccionar");
        botonSeleccionar.setOnAction(e -> {
            tablaContenedor.setVisible(true);
            if (onSeleccionarListener != null) {
                onSeleccionarListener.accept(columna, this);
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

        HBox fila = new HBox(10, botonSeleccionar, campoPersonalizado);
        fila.setAlignment(Pos.CENTER_LEFT);

        seleccionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        tablaContenedor.setVisible(false);
        tablaContenedor.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1px;");

        this.getChildren().addAll(fila, seleccionLabel, tablaContenedor);
    }

    private void actualizarLabel(String valor) {
        String bonito = columna.substring(0, 1).toUpperCase() + columna.substring(1);
        seleccionLabel.setText(bonito + " seleccionado: " + valor);
    }

    public void setValorDesdeTabla(String valor) {
        System.out.println("🟢 Valor seleccionado: " + valor);
        campoPersonalizado.setText(valor);
        actualizarLabel(valor);
        tablaContenedor.setVisible(false);
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
        return tabla;
    }

    public String getColumna() {
        return columna;
    }
}
