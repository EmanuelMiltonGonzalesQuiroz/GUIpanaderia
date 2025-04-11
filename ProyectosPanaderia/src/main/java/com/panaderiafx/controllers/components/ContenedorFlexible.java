package com.panaderiafx.controllers.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ContenedorFlexible extends StackPane {

    public ContenedorFlexible() {
        this.setStyle("-fx-background-color: #FAF3E0;");
        this.setAlignment(Pos.CENTER);
    }

    public void setContenido(Node contenido) {
        this.getChildren().clear();
        VBox contenedor = new VBox(contenido);
        contenedor.setAlignment(Pos.CENTER);
        this.getChildren().add(contenedor);
    }
}
