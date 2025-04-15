package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioDinamico;
import com.panaderiafx.controllers.components.TablaBusquedaSimple;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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

        HBox contenedorGeneral = new HBox(30);
        contenedorGeneral.setPadding(new Insets(20));

        VBox contenedorFormulario = new VBox();
        contenedorFormulario.setPrefWidth(500);

        VBox contenedorTabla = new VBox(); // Aquí se insertará TablaInteractiva cuando se solicite
        contenedorTabla.setPrefWidth(400);

        FormularioDinamico formulario = new FormularioDinamico(tabla, definicionCampos);
        contenedorFormulario.getChildren().add(formulario);

        formulario.getCampos().values().forEach(nodo -> {
            if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                campoExtendido.setOnSeleccionarListener((columna, campo) -> {
                    contenedorTabla.getChildren().setAll(crearTabla(columna, campo));
                });
            }
        });

        contenedorGeneral.getChildren().addAll(contenedorFormulario, contenedorTabla);

        ScrollPane sc = new ScrollPane(contenedorGeneral);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }

    private static Node crearTabla(String columna, CampoSeleccionExtendido campoExtendido) {
        List<Map<String, String>> datos = VerUtils.verTabla(campoExtendido.getTabla());
    
        // Debug
        System.out.println("🧪 Claves detectadas: " + datos.get(0).keySet());
        System.out.println("🧪 Usando columna: " + columna);
    
        List<Map<String, String>> valoresUnicos = datos.stream()
            .map(row -> {
                String val = row.getOrDefault(columna, "").trim();
                return Map.of(columna, val);
            })
            .filter(row -> !row.get(columna).isBlank())
            .distinct()
            .toList();
    
        TablaBusquedaSimple tabla = new TablaBusquedaSimple(valoresUnicos, columna);
        
        tabla.setOnSeleccionar(valor -> {
            System.out.println("✅ Valor seleccionado desde tabla: " + valor);
            campoExtendido.setValorDesdeTabla(valor);
            campoExtendido.getContenedorTabla().setVisible(false);
        });
    
        campoExtendido.getContenedorTabla().getChildren().setAll(tabla);
        return tabla;
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
