package com.panaderiafx.controllers.components.forms;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.utils.VerUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.*;

public class TablaBusquedaValores {

    public static Node crear(CampoSeleccionExtendido campo, String columnasMostrarTexto) {
        String tablaOrigen = campo.getTabla();
        String columnaCargar = campo.getColumna();
        List<Map<String, String>> datosOriginal = VerUtils.verTabla(tablaOrigen);

        List<String> columnas = Arrays.stream(columnasMostrarTexto.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .toList();

        final List<Map<String, String>> datosFiltrados;

        if (columnas.size() == 1 && columnas.get(0).equals(columnaCargar)) {
            // ⚠️ Filtrar duplicados solo si mostrar == cargar
            String col = columnas.get(0);
            Set<String> unicos = new HashSet<>();
            List<Map<String, String>> filtrados = new ArrayList<>();
            for (Map<String, String> fila : datosOriginal) {
                String valor = fila.getOrDefault(col, "");
                if (unicos.add(valor)) {
                    Map<String, String> nuevo = new HashMap<>();
                    nuevo.put(col, valor);
                    filtrados.add(nuevo);
                }
            }
            datosFiltrados = filtrados;
        } else {
            // Si mostrar y cargar son distintos, permitir repetidos
            datosFiltrados = datosOriginal;
        }

        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for (String col : columnas) {
            TableColumn<Map<String, String>, String> columna = new TableColumn<>(col);
            columna.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(col, "")));
            tabla.getColumns().add(columna);
        }

        tabla.getItems().addAll(datosFiltrados);

        tabla.setOnMouseClicked(e -> {
            Map<String, String> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila != null) {
                campo.setValorDesdeTabla(fila.getOrDefault(columnaCargar, ""));
            }
        });

        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: #FFF9C4;" +
            "-fx-padding: 8px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #FBC02D;" +
            "-fx-border-width: 1.5px;"
        );

        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            String filtro = newVal.toLowerCase();
            tabla.getItems().setAll(
                datosFiltrados.stream()
                    .filter(fila -> columnas.stream()
                        .anyMatch(col -> fila.getOrDefault(col, "").toLowerCase().contains(filtro)))
                    .toList()
            );
        });

        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(true);
        scrollTabla.setFitToHeight(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollTabla, Priority.ALWAYS);

        VBox contenedor = new VBox(10, campoBusqueda, scrollTabla);
        VBox.setVgrow(contenedor, Priority.ALWAYS);
        return contenedor;
    }
}
