package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.librosemanal.VistaLibroSemanal;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class LibroSemanalController {

    public static Node crearVista() {
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        HBox contenedor = new HBox();
        Node vista = new VistaLibroSemanal();

        contenedor.getChildren().add(vista);
        layout.getChildren().add(contenedor);

        return layout;
    }
}
