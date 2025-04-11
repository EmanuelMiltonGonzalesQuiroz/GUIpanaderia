package com.panaderiafx.controllers.components;

import javafx.scene.control.Label;

public class EtiquetaPersonalizada extends Label {

    public EtiquetaPersonalizada(String texto) {
        super(texto + ":");
        this.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    }
}
