package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.TablaInteractiva;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class VerController {

    public static ScrollPane mostrar(String nombreTabla) {
        return mostrar(nombreTabla, null);
    }

    public static ScrollPane mostrar(String nombreTabla, List<String> columnasOcultas) {
        // ✅ Leer directamente desde Excel SIEMPRE
        List<Map<String, String>> datos = VerUtils.verTabla(nombreTabla);

        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay datos en la tabla: " + nombreTabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        Set<String> todas = datos.get(0).keySet();
        List<String> columnasVisibles = columnasOcultas == null
                ? new ArrayList<>(todas)
                : todas.stream()
                       .filter(c -> !columnasOcultas.contains(c))
                       .collect(Collectors.toList());

        TablaInteractiva tablaView = new TablaInteractiva(datos, columnasVisibles, 20);
        tablaView.setAncho(-1); // ocupar todo si no se define
        tablaView.setAlto(-1);  // ocupar todo si no se define

        ScrollPane scroll = new ScrollPane(tablaView);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return scroll;
    }
}
