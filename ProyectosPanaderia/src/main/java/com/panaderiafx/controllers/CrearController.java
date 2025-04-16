package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioDinamico;
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

public class CrearController {

    public static ScrollPane mostrar(String tabla) {
        List<Map<String, String>> datos = VerUtils.verTabla(tabla);
        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay estructura disponible para: " + tabla));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla);

        VBox contenedorVertical = new VBox(20);
        contenedorVertical.setStyle("-fx-background-color: #FFF3E0;");
        contenedorVertical.setPadding(new Insets(20));

        Label tituloPrincipal = new Label("Crear - " + tabla.substring(0, 1).toUpperCase() + tabla.substring(1));
        tituloPrincipal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        tituloPrincipal.setMaxWidth(Double.MAX_VALUE);
        tituloPrincipal.setAlignment(Pos.CENTER);

        HBox contenedorGeneral = new HBox(30);
        contenedorGeneral.setPadding(new Insets(20));

        VBox contenedorFormulario = new VBox(10);
        contenedorFormulario.setPrefWidth(500);

        Label tituloFormulario = new Label("Formulario de creación");
        tituloFormulario.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contenedorFormulario.getChildren().add(tituloFormulario);

        VBox contenedorTabla = new VBox(10);
        contenedorTabla.setPrefWidth(400);

        Label tituloTabla = new Label("Selección de valores");
        tituloTabla.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloTabla.setVisible(false);
        contenedorTabla.getChildren().add(tituloTabla);

        FormularioDinamico formulario = new FormularioDinamico(tabla, definicionCampos);
        contenedorFormulario.getChildren().add(formulario);

        formulario.getCampos().values().forEach(nodo -> {
            if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                campoExtendido.setOnSeleccionarListener((columna, campo) -> {
                    tituloTabla.setVisible(true);
                    contenedorTabla.getChildren().removeIf(n -> n instanceof TablaBusquedaSimple);
                    contenedorTabla.getChildren().add(crearTabla(campoExtendido));
                });
            }
        });

        contenedorGeneral.getChildren().addAll(contenedorFormulario, contenedorTabla);
        contenedorVertical.getChildren().addAll(tituloPrincipal, contenedorGeneral);

        ScrollPane sc = new ScrollPane(contenedorVertical);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }

    private static Node crearTabla(CampoSeleccionExtendido campoExtendido) {
        String tablaOrigen = campoExtendido.getTabla();
        String columnaMostrar = campoExtendido.getColumna();

        List<Map<String, String>> datos = VerUtils.verTabla(tablaOrigen);
        List<Map<String, String>> valoresUnicos = datos.stream()
                .map(row -> Map.of(columnaMostrar, row.getOrDefault(columnaMostrar, "").trim()))
                .filter(row -> !row.get(columnaMostrar).isBlank())
                .distinct()
                .toList();

        TablaBusquedaSimple tabla = new TablaBusquedaSimple(valoresUnicos, columnaMostrar);
        tabla.setOnSeleccionar(valor -> campoExtendido.setValorDesdeTabla(valor));
        campoExtendido.getContenedorTabla().getChildren().setAll(tabla);
        return tabla;
    }

    private static List<Map<String, Object>> generarInstrucciones(String tabla) {
        List<Map<String, Object>> definicion = new ArrayList<>();
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónFormularios");

        for (Map<String, String> fila : config) {
            if (!fila.getOrDefault("Tabla", "").equalsIgnoreCase(tabla)) continue;

            Map<String, Object> campo = new HashMap<>();
            campo.put("nombre", fila.get("Campo"));
            campo.put("tipo", fila.get("Tipo").toLowerCase());

            if (fila.getOrDefault("Tipo", "").equalsIgnoreCase("select")) {
                campo.put("origen", fila.get("Origen"));
                campo.put("datoMostrar", fila.get("Dato a Mostrar"));
                campo.put("datoCargar", fila.get("Dato a Cargar"));
            }

            definicion.add(campo);
        }

        if (definicion.isEmpty()) {
            VBox error = new VBox(new Label("❌ No hay configuración definida para: " + tabla));
            error.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return List.of(Map.of("nombre", "ERROR", "tipo", "label"));
        }

        return definicion;
    }
}
