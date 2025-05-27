package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.editarproduccion.VistaEditarProduccion;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class RegistroIngresos {

    public static Node crearVista() {
        // Contenedor raíz con margen
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        // Contenedor horizontal principal
        HBox contenedor = new HBox();
        contenedor.setSpacing(40);
        contenedor.setPrefHeight(600);

        // Vista principal ya contiene todo (selector + formulario + ingredientes)
        Node vistaResumen = new VistaEditarProduccion();

        contenedor.getChildren().add(vistaResumen);
        layout.getChildren().add(contenedor);

        return layout;
    }
}
