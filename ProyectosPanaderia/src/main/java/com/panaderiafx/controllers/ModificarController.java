package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioModificar;
import com.panaderiafx.controllers.components.TablaBusquedaSimple;
import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class ModificarController {

    public static ScrollPane mostrar(String tabla) {
        List<Map<String, String>> registros = VerUtils.verTabla(tabla);
        if (registros.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay datos disponibles para modificar en: " + tabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }
    
        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla, registros.get(0));
    
        VBox contenedorGeneral = new VBox(20);
        contenedorGeneral.setPadding(new Insets(20));
        contenedorGeneral.setStyle("-fx-background-color: #FFF3E0;");
    
        Label tituloGeneral = new Label("Modificar - " + traducirNombreTabla(tabla));
        tituloGeneral.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        tituloGeneral.setAlignment(Pos.CENTER);
        tituloGeneral.setMaxWidth(Double.MAX_VALUE);
    
        HBox root = new HBox(30);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
    
        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPrefWidth(300);
        panelIzquierdo.setAlignment(Pos.TOP_CENTER);
    
        Label tituloIzq = new Label("Registros disponibles");
        tituloIzq.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        panelIzquierdo.getChildren().addAll(tituloIzq);
    
        String columnaBusqueda = registros.get(0).keySet().stream().skip(1).findFirst().orElseGet(() ->
                registros.get(0).keySet().iterator().next()
        );
    
        TablaBusquedaSimple tablaBusqueda = new TablaBusquedaSimple(registros, columnaBusqueda);
        panelIzquierdo.getChildren().add(tablaBusqueda);
    
        VBox panelFormulario = new VBox(10);
        panelFormulario.setPrefWidth(600);
    
        Label tituloCentro = new Label("Formulario de modificación");
        tituloCentro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloCentro.setVisible(false);
        panelFormulario.getChildren().add(tituloCentro);
    
        VBox panelDerecho = new VBox(10);
        panelDerecho.setPrefWidth(400);
    
        Label tituloDerecha = new Label("Selección de valores");
        tituloDerecha.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloDerecha.setVisible(false);
        panelDerecho.getChildren().add(tituloDerecha);
    
        tablaBusqueda.setOnSeleccionar(valor -> {
            Optional<Map<String, String>> filaOpt = registros.stream()
                    .filter(fila -> fila.getOrDefault(columnaBusqueda, "").equals(valor))
                    .findFirst();
    
            filaOpt.ifPresent(fila -> {
                panelFormulario.getChildren().removeIf(n -> n instanceof FormularioModificar);
                tituloCentro.setVisible(true);
    
                FormularioModificar formulario = new FormularioModificar(tabla, definicionCampos, registros, fila);
                panelFormulario.getChildren().add(formulario);
    
                formulario.getCampos().values().forEach(nodo -> {
                    if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                        campoExtendido.setOnSeleccionarListener((columna, campo) -> {
                            tituloDerecha.setVisible(true);
                            panelDerecho.getChildren().removeIf(n -> n instanceof TablaBusquedaSimple);
                            panelDerecho.getChildren().add(crearTabla(columna, campo));
                        });
                    }
                });
            });
        });
    
        root.getChildren().addAll(panelIzquierdo, panelFormulario, panelDerecho);
        contenedorGeneral.getChildren().addAll(tituloGeneral, root);
    
        ScrollPane sc = new ScrollPane(contenedorGeneral);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }
    
    private static Node crearTabla(String columna, CampoSeleccionExtendido campoExtendido) {
        List<Map<String, String>> datos = VerUtils.verTabla(campoExtendido.getTabla());

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
