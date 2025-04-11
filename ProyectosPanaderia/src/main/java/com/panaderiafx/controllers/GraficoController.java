package com.panaderiafx.controllers;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

public class GraficoController {
    public static ScrollPane mostrar(String tabla) {
        return new ScrollPane(new Label("Grafico Controller - " + tabla));
    }
}
