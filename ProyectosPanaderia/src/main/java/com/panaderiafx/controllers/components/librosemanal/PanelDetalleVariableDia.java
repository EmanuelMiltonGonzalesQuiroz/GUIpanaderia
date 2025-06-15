package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import com.panaderiafx.utils.cache.EditorTemporalCache;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PanelDetalleVariableDia {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

    public static void setFechaInicio(LocalDate fecha) {
        fechaInicio = fecha.with(java.time.DayOfWeek.MONDAY);
    }

    public static Node crear() {
        Map<String, Map<String, String>> cacheGuardado = EditorTemporalCache.getCambios("VariableDia");
        EditorTemporalCache.setCambios("VariableDia", cacheGuardado); // Reaplicar si había cambios

        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FFF59D; -fx-background-radius: 10;");

        Label titulo = new Label("DETALLE VARIABLES DEL DÍA");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        LocalDate finSemana = fechaInicio.plusDays(6);
        List<Map<String, String>> datosOriginales = VerUtils.verTabla("VariableDia").stream()
                .filter(f -> {
                    LocalDate fecha = ParseUtils.toDate(f.getOrDefault("Fecha", ""));
                    return fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(finSemana);
                })
                .toList();

        for (Map<String, String> fila : datosOriginales) {
            fila.putIfAbsent("EFECTO", "-");
        }

        EditorTemporalCache.aplicarCache("VariableDia", "Código", datosOriginales);
        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList(datosOriginales);

        TableView<Map<String, String>> tabla = new TableView<>(datos);
        tabla.setPrefHeight(300);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, String>, String> colCodigo = new TableColumn<>("Código");
        colCodigo.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Código", "")));

        TableColumn<Map<String, String>, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Fecha", "")));
        colFecha.setCellFactory(tc -> new TableCell<>() {
            private final ComboBox<LocalDate> combo = new ComboBox<>();

            {
                combo.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < tabla.getItems().size()) {
                        Map<String, String> fila = tabla.getItems().get(idx);
                        LocalDate seleccionada = combo.getValue();
                        if (seleccionada != null) {
                            fila.put("Fecha", seleccionada.format(FORMATO));
                            EditorTemporalCache.guardarFila("VariableDia", fila.get("Código"), fila);
                            tabla.refresh();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    List<LocalDate> semana = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {
                        semana.add(fechaInicio.plusDays(i));
                    }
                    combo.setItems(FXCollections.observableArrayList(semana));
                    combo.setValue(ParseUtils.toDate(item));
                    setGraphic(combo);
                }
            }
        });

        TableColumn<Map<String, String>, String> colVariable = new TableColumn<>("Valor");
        colVariable.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Valor", "0")));
        colVariable.setCellFactory(tc -> new TableCell<>() {
            private final TextField field = new TextField();

            {
                field.setOnAction(e -> commitEdit(field.getText()));
                field.focusedProperty().addListener((obs, was, now) -> {
                    if (!now) commitEdit(field.getText());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    field.setText(item);
                    setGraphic(field);
                }
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                int idx = getIndex();
                if (idx >= 0 && idx < tabla.getItems().size()) {
                    Map<String, String> fila = tabla.getItems().get(idx);
                    fila.put("Valor", newValue);
                    EditorTemporalCache.guardarFila("VariableDia", fila.get("Código"), fila);
                    recalcular(tabla.getItems());
                    tabla.refresh();
                }
            }
        });

        TableColumn<Map<String, String>, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Descripción", "")));
        colDescripcion.setCellFactory(tc -> new TableCell<>() {
            private final TextField field = new TextField();

            {
                field.setOnAction(e -> commitEdit(field.getText()));
                field.focusedProperty().addListener((obs, was, now) -> {
                    if (!now) commitEdit(field.getText());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    field.setText(item);
                    setGraphic(field);
                }
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                int idx = getIndex();
                if (idx >= 0 && idx < tabla.getItems().size()) {
                    Map<String, String> fila = tabla.getItems().get(idx);
                    fila.put("Descripción", newValue);
                    EditorTemporalCache.guardarFila("VariableDia", fila.get("Código"), fila);
                    tabla.refresh();
                }
            }
        });

        TableColumn<Map<String, String>, String> colEfecto = new TableColumn<>("EFECTO");
        colEfecto.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("EFECTO", "-")));
        colEfecto.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < tabla.getItems().size()) {
                        Map<String, String> fila = tabla.getItems().get(idx);
                        String actual = fila.getOrDefault("EFECTO", "-");
                        String nuevo = actual.equals("+") ? "-" : "+";
                        fila.put("EFECTO", nuevo);
                        EditorTemporalCache.guardarFila("VariableDia", fila.get("Código"), fila);
                        recalcular(tabla.getItems());
                        tabla.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    btn.setText(item);
                    setGraphic(btn);
                }
            }
        });

        // CAMBIO: Columna de producción ampliada con fecha y producto
        TableColumn<Map<String, String>, String> colProduccion = new TableColumn<>("Producción asociada");
        colProduccion.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Producción asociada", "-")));
        colProduccion.setPrefWidth(300); // Ampliado para mostrar más información
        colProduccion.setCellFactory(tc -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>();
            private final Map<String, ProduccionInfo> mapaProduccionInfo = new HashMap<>();

            {
                combo.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < tabla.getItems().size()) {
                        Map<String, String> fila = tabla.getItems().get(idx);
                        String seleccionado = combo.getValue();
                        if (seleccionado != null) {
                            // Extraer solo el código de producción (primera parte antes del " | ")
                            String codigoProduccion = seleccionado.split(" \\| ")[0];
                            fila.put("Producción asociada", codigoProduccion);
                            
                            // Actualizar fecha automáticamente
                            ProduccionInfo info = mapaProduccionInfo.get(codigoProduccion);
                            if (info != null) {
                                fila.put("Fecha", info.fecha);
                            }
                            
                            EditorTemporalCache.guardarFila("VariableDia", fila.get("Código"), fila);
                            tabla.refresh();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    List<Map<String, String>> producciones = VerUtils.verTabla("Produccion");
                    List<String> opciones = new ArrayList<>();
                    mapaProduccionInfo.clear();

                    // CAMBIO: Crear opciones con formato "Código | Fecha | Producto"
                    for (Map<String, String> prod : producciones) {
                        String codigo = prod.getOrDefault("Código Producción", "-");
                        String fecha = prod.getOrDefault("Fecha", "-");
                        String producto = prod.getOrDefault("Producto", "-");
                        
                        LocalDate f = ParseUtils.toDate(fecha);
                        if (f != null && !f.isBefore(fechaInicio) && !f.isAfter(fechaInicio.plusDays(6))) {
                            // Formato mejorado: "PRO0001 | 18/05/2025 | Sliced Bread"
                            String opcionDisplay = String.format("%s | %s | %s", codigo, fecha, producto);
                            opciones.add(opcionDisplay);
                            
                            // Guardar información para uso posterior
                            mapaProduccionInfo.put(codigo, new ProduccionInfo(fecha, producto));
                        }
                    }

                    combo.setItems(FXCollections.observableArrayList(opciones));

                    // Seleccionar el valor actual
                    Map<String, String> fila = tabla.getItems().get(getIndex());
                    String codigoActual = fila.getOrDefault("Producción asociada", "-");
                    
                    // Buscar la opción que corresponde al código actual
                    String opcionActual = opciones.stream()
                            .filter(opcion -> opcion.startsWith(codigoActual + " | "))
                            .findFirst()
                            .orElse("-");
                    
                    combo.setValue(opcionActual);
                    setGraphic(combo);
                }
            }
        });

        tabla.getColumns().addAll(colCodigo, colFecha, colVariable, colDescripcion, colProduccion, colEfecto);

        Button botonAgregar = new Button("+ Agregar");
        botonAgregar.setOnAction(e -> {
            String codigo = CodigoGenerator.generarCodigo("VariableDia", "Código");
            String fecha = fechaInicio.format(FORMATO);
            Map<String, String> nueva = new HashMap<>();
            nueva.put("Código", codigo); 
            nueva.put("Fecha", fecha);
            nueva.put("Descripción", "Nuevo gasto");
            nueva.put("Valor", "0");
            nueva.put("Producción asociada", "-");
            nueva.put("EFECTO", "-");
            tabla.getItems().add(nueva);
            EditorTemporalCache.guardarFila("VariableDia", codigo, nueva);
            recalcular(tabla.getItems());
        });

        contenedor.getChildren().addAll(titulo, tabla, botonAgregar);
        recalcular(tabla.getItems());
        return contenedor;
    }

    private static void recalcular(List<Map<String, String>> filas) {
        double total = 0;
        for (Map<String, String> fila : filas) {
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
            String efecto = fila.getOrDefault("EFECTO", "-");
            if (efecto.equals("+")) total += valor;
            else total -= valor;
        }
        CacheLibroSemanal.set(Tipo.COSTOS_DIA, total);
    }
    
    /**
     * NUEVO: Clase para almacenar información de producción
     */
    private static class ProduccionInfo {
        final String fecha;
        final String producto;
        
        ProduccionInfo(String fecha, String producto) {
            this.fecha = fecha;
            this.producto = producto;
        }
    }
}