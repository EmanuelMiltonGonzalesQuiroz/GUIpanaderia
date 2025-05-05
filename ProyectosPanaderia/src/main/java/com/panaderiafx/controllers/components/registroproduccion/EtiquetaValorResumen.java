package com.panaderiafx.controllers.components.registroproduccion;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class EtiquetaValorResumen extends HBox {

    private final TextField campoValor;

    public EtiquetaValorResumen(String texto, double valor) {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);

        Label etiqueta = new Label(texto);
        etiqueta.setPrefWidth(200);
        etiqueta.setStyle(defineEstiloEtiqueta(texto));

        campoValor = new TextField(String.format("BZD %.2f", valor));
        campoValor.setEditable(false);
        campoValor.setPrefWidth(100);
        campoValor.setStyle("-fx-background-radius: 6; -fx-alignment: center-right;");

        this.getChildren().addAll(etiqueta, campoValor);
    }

    private String defineEstiloEtiqueta(String texto) {
        
            return "-fx-background-color: #FFECB3; " +
                   "-fx-background-radius: 8; " +
                   "-fx-padding: 5 10 5 10; " +
                   "-fx-font-weight: bold; " +
                   "-fx-text-fill: black; " +
                   "-fx-cursor: hand;";
    }
}
