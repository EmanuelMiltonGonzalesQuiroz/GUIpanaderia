package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.TablaInteractiva;
import com.panaderiafx.utils.RelacionadorVisual;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.*;

public class VerController {

    public static ScrollPane mostrar(String nombreTabla) {
        return mostrar(nombreTabla, nombreTabla, null);
    }

    public static ScrollPane mostrar(String nombreTabla, String nombreVisible) {
        return mostrar(nombreTabla, nombreVisible, null);
    }

    public static ScrollPane mostrar(String nombreTabla, String nombreVisible, List<String> columnasOcultas) {
        List<Map<String, String>> datosOriginales = VerUtils.verTabla(nombreTabla);

        if (datosOriginales.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay datos en la tabla: " + nombreVisible));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, String>> datos = datosOriginales;

        Set<String> tablasConVista = new HashSet<>(List.of(
                "RecetasIngredientes", "Producción", "Costos", "TabladeConversión"
        ));

        if (tablasConVista.contains(nombreTabla)) {
            datos = RelacionadorVisual.aplicarSustituciones(nombreTabla, datosOriginales);
        }

        Set<String> todas = datos.get(0).keySet();
        List<String> columnasVisibles = columnasOcultas == null
                ? new ArrayList<>(todas)
                : todas.stream().filter(c -> !columnasOcultas.contains(c)).toList();

        TablaInteractiva tablaView = new TablaInteractiva(datos, columnasVisibles, 20, nombreTabla);
        tablaView.setAncho(-1);
        tablaView.setAlto(-1);

        Label titulo = new Label("Tabla - " + nombreVisible);
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        VBox contenedor = new VBox(20, titulo, tablaView);
        contenedor.setStyle("-fx-background-color: #FFF3E0;");
        contenedor.setAlignment(Pos.TOP_CENTER);
        contenedor.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return scroll;
    }
}
