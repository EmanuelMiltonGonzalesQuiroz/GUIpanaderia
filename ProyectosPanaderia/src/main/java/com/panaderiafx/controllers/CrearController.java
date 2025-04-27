package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioDinamico;
import com.panaderiafx.utils.VerUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.*;

public class CrearController {

    public static ScrollPane mostrar(String tabla) {
        return mostrar(tabla, tabla);
    }

    public static ScrollPane mostrar(String tabla, String nombreVisible) {
        List<Map<String, String>> datos = VerUtils.verTabla(tabla);
        if (datos.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay estructura disponible para: " + nombreVisible));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla);

        VBox contenedorVertical = new VBox(20);
        contenedorVertical.setStyle("-fx-background-color: #FFF3E0;");
        contenedorVertical.setPadding(new Insets(20));

        Label tituloPrincipal = new Label("Crear - " + nombreVisible);
        tituloPrincipal.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        tituloPrincipal.setMaxWidth(Double.MAX_VALUE);
        tituloPrincipal.setAlignment(Pos.CENTER);

        HBox contenedorGeneral = new HBox(30);
        contenedorGeneral.setPadding(new Insets(20));

        VBox contenedorFormulario = new VBox(10);
        contenedorFormulario.setPrefWidth(400);

        Label tituloFormulario = new Label("Formulario de creación");
        tituloFormulario.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        contenedorFormulario.getChildren().add(tituloFormulario);

        VBox contenedorTabla = new VBox(10);
        contenedorTabla.setPrefWidth(600);

        Label tituloTabla = new Label("Selección de valores");
        tituloTabla.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloTabla.setVisible(false);
        contenedorTabla.getChildren().add(tituloTabla);

        FormularioDinamico formulario = new FormularioDinamico(tabla, definicionCampos);
        contenedorFormulario.getChildren().add(formulario);

        formulario.getCampos().values().forEach(nodo -> {
            if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                campoExtendido.setOnSeleccionarListener((columnasMostrarTexto, campo) -> {
                    tituloTabla.setVisible(true);
                    contenedorTabla.getChildren().removeIf(n -> n instanceof VBox);
                    contenedorTabla.getChildren().add(crearTablaConBusqueda(campoExtendido, columnasMostrarTexto));
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

    private static Node crearTablaConBusqueda(CampoSeleccionExtendido campoExtendido, String columnasMostrarTexto) {
        String tablaOrigen = campoExtendido.getTabla();
        List<String> columnas = Arrays.stream(columnasMostrarTexto.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .toList();
    
        List<Map<String, String>> datosOriginal = VerUtils.verTabla(tablaOrigen);
    
        // ⚡ Filtrar duplicados si solo es una columna
        List<Map<String, String>> datosFinal;
        if (columnas.size() == 1) {
            String columnaUnica = columnas.get(0);
            Set<String> valoresUnicos = new LinkedHashSet<>();
            for (Map<String, String> fila : datosOriginal) {
                valoresUnicos.add(fila.getOrDefault(columnaUnica, ""));
            }
            datosFinal = new ArrayList<>();
            for (String valor : valoresUnicos) {
                Map<String, String> fila = new HashMap<>();
                fila.put(columnaUnica, valor);
                datosFinal.add(fila);
            }
        } else {
            datosFinal = datosOriginal;
        }
    
        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(Region.USE_COMPUTED_SIZE);
        tabla.setMaxHeight(Double.MAX_VALUE);
        tabla.setMinHeight(200);
    
        for (String col : columnas) {
            TableColumn<Map<String, String>, String> columna = new TableColumn<>(col);
            columna.setCellValueFactory(cellData -> {
                String valor = cellData.getValue().getOrDefault(col, "");
                return new SimpleStringProperty(valor);
            });
            tabla.getColumns().add(columna);
        }
    
        tabla.getItems().addAll(datosFinal);
    
        tabla.setOnMouseClicked(event -> {
            Map<String, String> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila != null) {
                String valorCargar = fila.getOrDefault(campoExtendido.getColumna(), "");
                campoExtendido.setValorDesdeTabla(valorCargar);
            }
        });
    
        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: #FFF9C4;
            -fx-padding: 8px;
            -fx-border-radius: 5px;
            -fx-background-radius: 5px;
            -fx-border-color: #FBC02D;
            -fx-border-width: 1.5px;
        """);
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);
    
        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            tabla.getItems().clear();
            tabla.getItems().addAll(datosFinal.stream()
                .filter(map -> columnas.stream()
                    .anyMatch(col -> map.getOrDefault(col, "").toLowerCase().contains(newVal.toLowerCase()))
                ).toList());
        });
    
        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setFitToHeight(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setMaxHeight(Double.MAX_VALUE);
    
        VBox contenedor = new VBox(10, campoBusqueda, scrollTabla);
        VBox.setVgrow(scrollTabla, Priority.ALWAYS);
        contenedor.setMaxHeight(Double.MAX_VALUE);
        contenedor.setFillWidth(true);
    
        return contenedor;
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
