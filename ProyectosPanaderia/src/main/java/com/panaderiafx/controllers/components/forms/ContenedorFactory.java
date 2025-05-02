package com.panaderiafx.controllers.components.forms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ContenedorFactory {

    public static VBox crearContenedorFormulario(String tituloTexto) {
        VBox contenedor = new VBox(10);
        contenedor.setPrefWidth(400);
        Label titulo = new Label(tituloTexto);
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contenedor.getChildren().add(titulo);
        return contenedor;
    }

    public static VBox crearContenedorTabla(String tituloTexto) {
        VBox contenedor = new VBox(10);
        contenedor.setPrefWidth(600);
        Label titulo = new Label(tituloTexto);
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titulo.setVisible(false);
        contenedor.getChildren().add(titulo);
        return contenedor;
    }

    public static VBox crearContenedorPrincipal(String tituloGeneral) {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-background-color: #FFF3E0;");
        contenedor.setPadding(new Insets(20));

        Label titulo = new Label(tituloGeneral);
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        contenedor.getChildren().add(titulo);
        return contenedor;
    }
}
