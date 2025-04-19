package com.panaderiafx.controllers.components.table;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.IntConsumer;

public class ControlPaginacion extends HBox {

    private final Button anterior = new Button("⬅ Anterior");
    private final Button siguiente = new Button("Siguiente ➡");

    public ControlPaginacion(IntConsumer onCambioPagina, Label infoPagina) {
        anterior.setOnAction(e -> onCambioPagina.accept(-1));
        siguiente.setOnAction(e -> onCambioPagina.accept(1));

        this.getChildren().addAll(anterior, infoPagina, siguiente);
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
    }

    public void mostrarBotones(boolean mostrar) {
        this.setVisible(mostrar);
        this.setManaged(mostrar);
    }
}
