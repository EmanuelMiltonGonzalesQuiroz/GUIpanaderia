package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.FormularioModificar;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

public class ModificarController {

    public static ScrollPane mostrar(String tabla) {
        List<Map<String, String>> datos = VerUtils.verTabla(tabla);
        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay datos disponibles para modificar en: " + tabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla, datos.get(0));
        ScrollPane sc = new ScrollPane(new FormularioModificar(tabla, definicionCampos, datos));
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }

    private static List<Map<String, Object>> generarInstrucciones(String tabla, Map<String, String> ejemplo) {
        List<Map<String, Object>> definicion = new ArrayList<>();

        switch (tabla.toLowerCase()) {
            case "ingredientes" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    switch (campo.toLowerCase()) {
                        case "unidad", "categoria" -> item.put("tipo", "select");
                        case "fecha de actualización", "código" -> item.put("tipo", "label");
                        case "precio local" -> item.put("tipo", "precio_local");
                        case "precio dólar" -> item.put("tipo", "precio_dolar");
                        default -> item.put("tipo", "text");
                    }
                    definicion.add(item);
                }
            }
            case "recetas" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    switch (campo.toLowerCase()) {
                        case "producto", "ingrediente", "unidades" -> item.put("tipo", "select");
                        case "código receta" -> item.put("tipo", "label");
                        default -> item.put("tipo", "text");
                    }
                    definicion.add(item);
                }
            }
            case "produccion" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    item.put("tipo", campo.toLowerCase().contains("fecha") ? "fecha" : "text");
                    definicion.add(item);
                }
            }
            case "parametros" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    item.put("tipo", campo.equalsIgnoreCase("Nombre") ? "label" : "text");
                    definicion.add(item);
                }
            }
        }

        return definicion;
    }
}
