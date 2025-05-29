package com.panaderiafx.controllers.components.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.text.Normalizer;
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

        List<String> terminos = Arrays.stream(filtro.split(","))
                .map(String::trim)
                .map(TablaUtils::normalizarTexto)
                .filter(s -> s.length() >= 3) // ⚠️ dejar 3 como mínimo razonable
                .toList();

        if (terminos.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        return FXCollections.observableArrayList(
                datos.stream()
                        .filter(map -> {
                            for (String termino : terminos) {
                                for (String val : map.values()) {
                                    if (val != null && normalizarTexto(val).contains(termino)) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList())
        );
    }

    public static ObservableList<Map<String, String>> filtrarConColumnas(List<Map<String, String>> datos, String filtro, List<String> columnasFiltrables) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return FXCollections.observableArrayList(datos);
        }

        List<String> terminos = Arrays.stream(filtro.split(","))
                .map(String::trim)
                .map(TablaUtils::normalizarTexto)
                .filter(s -> s.length() >= 3)
                .toList();

        if (terminos.isEmpty()) {
            return FXCollections.observableArrayList();
        }

        return FXCollections.observableArrayList(
                datos.stream()
                        .filter(map -> {
                            // ✅ TODOS los términos deben estar en alguna columna de la fila
                            return terminos.stream().allMatch(termino ->
                                    columnasFiltrables.stream().anyMatch(columna -> {
                                        String val = map.get(columna);
                                        return val != null && normalizarTexto(val).contains(termino);
                                    })
                            );
                        })
                        .collect(Collectors.toList())
        );
    }


    private static String normalizarTexto(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }
}
