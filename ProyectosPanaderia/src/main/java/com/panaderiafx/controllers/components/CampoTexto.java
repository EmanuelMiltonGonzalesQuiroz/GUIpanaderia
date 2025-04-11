package com.panaderiafx.controllers.components;

import javafx.scene.control.TextField;

public class CampoTexto extends TextField {

    public CampoTexto() {
        this("");
    }

    public CampoTexto(String placeholder) {
        super();
        this.setPromptText(placeholder);
        this.setStyle("-fx-font-size: 14px;");
        this.setMaxWidth(Double.MAX_VALUE);
    }
}
