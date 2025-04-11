package com.panaderiafx.controllers.components;

import javafx.scene.control.Button;

public class BotonAccion extends Button {

    public BotonAccion(String texto, String color) {
        super(texto);
        this.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px;");
        this.setPrefWidth(120);
    }
}
