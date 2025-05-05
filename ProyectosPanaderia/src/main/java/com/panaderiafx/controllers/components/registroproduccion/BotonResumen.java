package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class BotonResumen extends HBox {
    public BotonResumen(String texto, TextField campoValor, String colorFondo) {
        setSpacing(10);

        Button boton = new Button(texto);
        boton.setStyle("-fx-font-weight: bold; -fx-background-color: " + colorFondo + "; -fx-background-radius: 6;");
        boton.setPrefWidth(160);

        campoValor.setEditable(false);
        campoValor.setPrefWidth(100);
        campoValor.setStyle("-fx-background-radius: 6; -fx-alignment: center-right;");

        getChildren().addAll(boton, campoValor);
    }

    public void setOnAction(javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        if (getChildren().get(0) instanceof Button boton) {
            boton.setOnAction(handler);
        }
    }
}
