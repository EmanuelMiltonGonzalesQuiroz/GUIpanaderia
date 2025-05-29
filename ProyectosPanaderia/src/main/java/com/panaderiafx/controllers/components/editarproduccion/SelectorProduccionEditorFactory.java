package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.utils.VerUtilsOptimized;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SelectorProduccionEditorFactory {

    public static VBox crearSelector(Consumer<Map<String, String>> onSeleccionar) {
        VBox contenedor = new VBox(10);
        contenedor.setPrefWidth(500);

        Label label = new Label("Seleccionar producción por fecha:");
        ComboBox<String> comboFechas = new ComboBox<>();
        TableView<Map<String, String>> tabla = new TableView<>();

        // 🚀 Cargar datos iniciales
        List<Map<String, String>> producciones = cargarProduccionesConVersion();
        List<String> fechasUnicas = obtenerFechasOrdenadas(producciones);

        comboFechas.setItems(FXCollections.observableArrayList(fechasUnicas));

        if (!fechasUnicas.isEmpty()) {
            String ultimaFecha = fechasUnicas.get(fechasUnicas.size() - 1);
            comboFechas.setValue(ultimaFecha);
            List<Map<String, String>> filtrados = actualizarTabla(tabla, producciones, ultimaFecha);
            if (!filtrados.isEmpty()) {
                Map<String, String> seleccionada = filtrados.get(filtrados.size() - 1);
                tabla.getSelectionModel().select(seleccionada);
                onSeleccionar.accept(seleccionada);
            }
        }

        // ✅ AL CAMBIAR LA FECHA, RECARGAR DESDE EXCEL
        comboFechas.setOnAction(e -> {
            String fechaSeleccionada = comboFechas.getValue();
            if (fechaSeleccionada != null) {
                List<Map<String, String>> produccionesActualizadas = cargarProduccionesConVersion();
                actualizarTabla(tabla, produccionesActualizadas, fechaSeleccionada);
            }
        });

        tabla.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Map<String, String> seleccionada = tabla.getSelectionModel().getSelectedItem();
                if (seleccionada != null) {
                    onSeleccionar.accept(seleccionada);
                }
            }
        });

        // 🧱 Columnas
        TableColumn<Map<String, String>, String> colCod = new TableColumn<>("Código Producción");
        colCod.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().get("Código Producción")));
        colCod.setMinWidth(150);

        TableColumn<Map<String, String>, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().get("Producto")));
        colProd.setMinWidth(130);

        TableColumn<Map<String, String>, String> colVer = new TableColumn<>("Versión");
        colVer.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getOrDefault("Versión", "")));
        colVer.setMinWidth(80);

        TableColumn<Map<String, String>, String> colMez = new TableColumn<>("Mezcla");
        colMez.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getOrDefault("Mezcla", "")));
        colMez.setMinWidth(80);

        TableColumn<Map<String, String>, String> colGan = new TableColumn<>("Ganancia Total");
        colGan.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().getOrDefault("Ganancia Tota", "")));
        colGan.setMinWidth(150);

        tabla.getColumns().addAll(colCod, colProd, colVer, colMez, colGan);
        tabla.setPrefHeight(500);
        tabla.setPrefWidth(460);
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        contenedor.getChildren().addAll(label, comboFechas, tabla);
        return contenedor;
    }

    private static List<Map<String, String>> actualizarTabla(
            TableView<Map<String, String>> tabla,
            List<Map<String, String>> producciones,
            String fecha) {
        List<Map<String, String>> filtrados = producciones.stream()
                .filter(p -> fecha.equals(p.get("Fecha")))
                .toList();
        tabla.setItems(FXCollections.observableArrayList(filtrados));
        return filtrados;
    }

    private static List<Map<String, String>> cargarProduccionesConVersion() {
        // 🔁 Forzar recarga desde archivo
        List<Map<String, String>> produccionesRaw = VerUtilsOptimized.actualizar("Produccion");

        Map<String, String> versionesPorReceta = VerUtilsOptimized.verTabla("Recetas").stream()
                .filter(r -> r.containsKey("Código receta") && r.containsKey("Versión"))
                .collect(Collectors.toMap(
                        r -> r.get("Código receta"),
                        r -> r.get("Versión"),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        return produccionesRaw.stream()
                .map(p -> {
                    Map<String, String> fila = new LinkedHashMap<>(p);
                    String codReceta = p.getOrDefault("Código Receta", "");
                    fila.put("Versión", versionesPorReceta.getOrDefault(codReceta, ""));
                    return fila;
                })
                .toList();
    }

    private static List<String> obtenerFechasOrdenadas(List<Map<String, String>> producciones) {
        return producciones.stream()
                .map(p -> p.getOrDefault("Fecha", ""))
                .filter(f -> !f.isEmpty())
                .distinct()
                .sorted(Comparator.comparing(f -> {
                    try {
                        return new SimpleDateFormat("dd/MM/yyyy").parse(f);
                    } catch (ParseException e) {
                        return new Date(0);
                    }
                }))
                .toList();
    }
}
