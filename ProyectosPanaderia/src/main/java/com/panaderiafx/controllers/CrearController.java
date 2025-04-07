package com.panaderiafx.controllers;

import com.panaderiafx.utils.*;
import com.panaderiafx.builder.FormularioBuilder;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

import java.util.*;

public class CrearController {

    public static VBox mostrar(String tabla) {
        VBox contenedor = new VBox(20);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setStyle("-fx-padding: 30; -fx-background-color: #FFF8E1;");
        contenedor.setPrefWidth(600);

        Label titulo = new Label("Formulario para crear en: " + tabla);
        titulo.setFont(new Font(20));

        Map<String, Control> campos = new LinkedHashMap<>();
        VBox formulario = FormularioBuilder.crearFormulario(tabla, campos);

        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");

        Button guardar = new Button("Guardar");
        guardar.setFont(new Font(16));
        guardar.setOnAction(e -> AccionGuardar.ejecutar(tabla, campos, error));

        contenedor.getChildren().addAll(titulo, formulario, error, guardar);
        return contenedor;
    }
}
