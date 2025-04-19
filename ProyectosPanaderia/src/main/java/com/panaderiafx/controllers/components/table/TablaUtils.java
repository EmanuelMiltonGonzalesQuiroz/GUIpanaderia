package com.panaderiafx.controllers.components.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.*;
import java.util.stream.Collectors;

public class TablaUtils {

    public static List<String> determinarColumnasVisibles(List<Map<String, String>> datos, List<String> columnas) {
        if (columnas == null || columnas.isEmpty()) {
            return datos.isEmpty() ? new ArrayList<>() : new ArrayList<>(datos.get(0).keySet());
        } else {
            return columnas;
        }
    }

    public static void configurarEstiloFilas(TableView<Map<String, String>> tabla) {
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Map<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #FFFFFF;");
                } else {
                    setStyle("-fx-background-color: #FFF8E1;");
                }
            }
        });
    }

    public static ObservableList<Map<String, String>> filtrar(List<Map<String, String>> datos, String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return FXCollections.observableArrayList(datos);
        }

        String buscar = filtro.toLowerCase();
        return FXCollections.observableArrayList(
                datos.stream()
                        .filter(map -> map.values().stream()
                                .anyMatch(val -> val.toLowerCase().contains(buscar)))
                        .collect(Collectors.toList())
        );
    }
}
