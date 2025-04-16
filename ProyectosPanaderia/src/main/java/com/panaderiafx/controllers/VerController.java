package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.TablaInteractiva;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

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
        tablaView.setAncho(-1);
        tablaView.setAlto(-1);

        Label titulo = new Label("Tabla - " + traducirNombreTabla(nombreTabla));
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

    private static String traducirNombreTabla(String tabla) {
        return switch (tabla.toLowerCase()) {
            case "ingredientes" -> "Ingredientes";
            case "recetas" -> "Recetas";
            case "produccion" -> "Producción";
            case "costos" -> "Costos";
            case "tasacambio" -> "Tasa de Cambio";
            case "historial de precios" -> "Historial de Precios";
            case "parametros" -> "Parámetros";
            case "tabladeconversión" -> "Tabla de Conversión";
            default -> tabla;
        };
    }
}
