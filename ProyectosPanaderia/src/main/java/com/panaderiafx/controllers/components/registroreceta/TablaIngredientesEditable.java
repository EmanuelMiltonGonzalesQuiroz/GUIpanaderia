package com.panaderiafx.controllers.components.registroreceta;

import com.panaderiafx.utils.VerUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

public class TablaIngredientesEditable {

    private final TableView<Map<String, String>> tabla = new TableView<>();
    private final ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();

    private final List<String> listaIngredientes;
    private final List<String> listaUnidades;
    private final Map<String, String> mapaNombreACodigo;

    public TablaIngredientesEditable() {
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        // Mapear nombre → código
        this.mapaNombreACodigo = ingredientes.stream()
                .collect(Collectors.toMap(
                        m -> m.getOrDefault("Nombre", "").trim(),
                        m -> m.getOrDefault("Código", "").trim(),
                        (v1, v2) -> v1
                ));

        // Lista de nombres únicos
        this.listaIngredientes = ingredientes.stream()
                .map(m -> m.getOrDefault("Nombre", "").trim())
                .filter(n -> !n.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Lista de unidades únicas desde Ingredientes
        this.listaUnidades = ingredientes.stream()
                .map(m -> m.getOrDefault("Unidad", "").trim())
                .filter(n -> !n.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        tabla.setItems(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(400);
        tabla.setMinWidth(500);

        crearColumnas();
    }

    private void crearColumnas() {
        TableColumn<Map<String, String>, String> colIngrediente = new TableColumn<>("Ingrediente");
        colIngrediente.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOrDefault("IngredienteNombre", "")
        ));
        colIngrediente.setCellFactory(col -> crearComboBoxEditable("Ingrediente", listaIngredientes));

        TableColumn<Map<String, String>, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrDefault("Cantidad", "")));
        colCantidad.setCellFactory(col -> crearTextFieldEditable("Cantidad"));

        TableColumn<Map<String, String>, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrDefault("Unidades", "")));
        colUnidad.setCellFactory(col -> crearComboBoxEditable("Unidades", listaUnidades));

        TableColumn<Map<String, String>, String> colAcciones = new TableColumn<>("✖");
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑");

            {
                btn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    datos.remove(fila);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colIngrediente, colCantidad, colUnidad, colAcciones);
    }

    private TableCell<Map<String, String>, String> crearTextFieldEditable(String campo) {
        return new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.textProperty().addListener((obs, old, val) -> {
                    if (getIndex() >= 0 && getIndex() < datos.size()) {
                        datos.get(getIndex()).put(campo, val.trim());
                    }
                });
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) setGraphic(null);
                else {
                    textField.setText(val);
                    setGraphic(textField);
                }
            }
        };
    }

    private TableCell<Map<String, String>, String> crearComboBoxEditable(String campo, List<String> opciones) {
        return new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>();

            {
                combo.setItems(FXCollections.observableArrayList(opciones));
                combo.setEditable(false);
                combo.valueProperty().addListener((obs, old, val) -> {
                    if (getIndex() >= 0 && getIndex() < datos.size()) {
                        if ("Ingrediente".equals(campo)) {
                            datos.get(getIndex()).put("IngredienteNombre", val != null ? val.trim() : "");
                            datos.get(getIndex()).put("Ingrediente", mapaNombreACodigo.getOrDefault(val.trim(), ""));
                        } else {
                            datos.get(getIndex()).put(campo, val != null ? val.trim() : "");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) setGraphic(null);
                else {
                    combo.setValue(val);
                    setGraphic(combo);
                }
            }
        };
    }

    public void agregarFilaVacia() {
        Map<String, String> nueva = new LinkedHashMap<>();
        nueva.put("Ingrediente", "");
        nueva.put("IngredienteNombre", "");
        nueva.put("Cantidad", "");
        nueva.put("Unidades", "");
        datos.add(nueva);
    }

    public Node getNodeConBoton() {
        VBox contenedor = new VBox(10);
        contenedor.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 10; -fx-border-color: #FF9800; -fx-border-width: 2;");
        Button btnAgregar = new Button("➕ Añadir ingrediente");
        btnAgregar.setOnAction(e -> agregarFilaVacia());
        btnAgregar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        contenedor.getChildren().addAll(tabla, btnAgregar);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return contenedor;
    }

    public List<Map<String, String>> getFilas() {
        return new ArrayList<>(datos);
    }

    public void limpiarTabla() {
        datos.clear();
        agregarFilaVacia();
    }
}
