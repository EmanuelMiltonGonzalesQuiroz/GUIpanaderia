package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.FormularioDinamico;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

public class CrearController {

    public static ScrollPane mostrar(String tabla) {
        List<Map<String, String>> datos = VerUtils.verTabla(tabla);
        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay estructura disponible para: " + tabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla, datos.get(0));
        ScrollPane sc = new ScrollPane(new FormularioDinamico(tabla, definicionCampos));
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
                        case "código" -> item.put("tipo", "codigo");
                        case "unidad", "categoria" -> item.put("tipo", "select");
                        case "precio local" -> item.put("tipo", "precio_local");
                        case "precio dólar" -> item.put("tipo", "precio_dolar");
                        case "fecha de actualización" -> item.put("tipo", "fecha");
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
                        case "código receta" -> item.put("tipo", "codigo");
                        case "producto", "ingrediente", "unidades" -> item.put("tipo", "select");
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
            case "costos" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    if (campo.equalsIgnoreCase("Tipo") || campo.equalsIgnoreCase("Moneda")) {
                        item.put("tipo", "select");
                    } else {
                        item.put("tipo", "text");
                    }
                    definicion.add(item);
                }
            }
            case "tasacambio" -> {
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
                    item.put("tipo", "text");
                    definicion.add(item);
                }
            }
            case "tabladeconversión" -> {
                for (String campo : ejemplo.keySet()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", campo);
                    if (campo.equalsIgnoreCase("Tipo de medida") || campo.equalsIgnoreCase("Unidad base")) {
                        item.put("tipo", "select");
                    } else {
                        item.put("tipo", "text");
                    }
                    definicion.add(item);
                }
            }
            default -> {
                VBox error = new VBox(new Label("❌ No se permite crear para: " + tabla));
                error.setStyle("-fx-alignment: center; -fx-padding: 20px;");
                return List.of(Map.of("nombre", "ERROR", "tipo", "label"));
            }
        }

        return definicion;
    }
}
