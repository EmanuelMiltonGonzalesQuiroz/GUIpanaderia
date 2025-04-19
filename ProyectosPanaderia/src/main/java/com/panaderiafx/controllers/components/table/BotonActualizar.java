package com.panaderiafx.controllers.components.table;

import javafx.scene.control.Button;

public class BotonActualizar extends Button {

    public BotonActualizar(Runnable accionActualizar) {
        super("\uD83D\uDD04 Actualizar");

        this.setStyle("""
            -fx-background-color: #FFA000;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 6px 12px;
            -fx-background-radius: 6px;
        """);

        this.setOnAction(e -> accionActualizar.run());
    }
}