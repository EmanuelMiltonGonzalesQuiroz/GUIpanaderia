package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.registroreceta.VistaCrearRecetaCompleta;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class RegistroRecetas {

    public static Node crearVista() {
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        // Contenedor horizontal con una sola columna por ahora
        HBox contenedor = new HBox();
        Node vista = VistaCrearRecetaCompleta.crearVista();

        contenedor.getChildren().add(vista);
        layout.getChildren().add(contenedor);

        return layout;
    }
}
