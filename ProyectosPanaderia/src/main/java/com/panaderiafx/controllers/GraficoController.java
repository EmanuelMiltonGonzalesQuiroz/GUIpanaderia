package com.panaderiafx.controllers;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class GraficoController {
    public static VBox mostrar(String tabla) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");
        layout.getChildren().add(new Label("Gráfico para: " + tabla));
        return layout;
    }
}
