package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.registroproduccion2.VistaRegistroProduccion;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class RegistroProduccion {

    public static Node crearVista() {
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        // Vista principal de registro de nueva producción
        Node vistaRegistro = VistaRegistroProduccion.crearVista();
        layout.getChildren().add(vistaRegistro);

        return layout;
    }
}
