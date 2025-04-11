package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.FormularioEliminar;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

public class EliminarController {

    public static ScrollPane mostrar(String tabla) {
        List<Map<String, String>> datos = VerUtils.verTabla(tabla);
        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay registros disponibles para eliminar en: " + tabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<String> columnas = new ArrayList<>(datos.get(0).keySet());
        ScrollPane sc = new ScrollPane(new FormularioEliminar(tabla, columnas, datos));
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }
}
