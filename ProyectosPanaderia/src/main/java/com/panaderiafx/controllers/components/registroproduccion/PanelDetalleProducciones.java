package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.FechaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PanelDetalleProducciones {

    public static void mostrar(VBox contenedor, String fecha, String tipo, BiConsumer<Double, Double> actualizarValores) {
        contenedor.getChildren().clear();

        Label titulo = new Label("Detalle de Producciones");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(300);

        TableColumn<Map<String, Object>, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("Producto")));

        TableColumn<Map<String, Object>, Double> colGanancia = new TableColumn<>("Ganancia");
        colGanancia.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>((Double) data.getValue().get("Ganancia")));

        TableColumn<Map<String, Object>, Double> colCosto = new TableColumn<>("Costo Directo");
        colCosto.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>((Double) data.getValue().get("Costo")));

        TableColumn<Map<String, Object>, Boolean> colCheck = new TableColumn<>("✓");
        colCheck.setCellValueFactory(data -> (SimpleBooleanProperty) data.getValue().get("Check"));
        colCheck.setCellFactory(tc -> {
            CheckBoxTableCell<Map<String, Object>, Boolean> cell = new CheckBoxTableCell<>();
            cell.setSelectedStateCallback(index -> {
                Map<String, Object> item = tabla.getItems().get(index);
                return (SimpleBooleanProperty) item.get("Check");
            });
            return cell;
        });

        tabla.getColumns().addAll(colProducto, colGanancia, colCosto, colCheck);

        List<Map<String, String>> producciones = VerUtils.verTabla("Produccion").stream()
                .filter(f -> FechaUtils.coincide(f.get("Fecha"), fecha, tipo))
                .collect(Collectors.toList());

        Map<String, String> recetas = VerUtils.verTabla("Recetas").stream()
                .collect(Collectors.toMap(
                        r -> r.getOrDefault("Código", ""),
                        r -> r.getOrDefault("Producto", "")
                ));

        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();

        for (Map<String, String> fila : producciones) {
            String codigoReceta = fila.getOrDefault("Receta", "");
            String producto = recetas.getOrDefault(codigoReceta, "(Desconocido)");
            double ganancia = ParseUtils.toDouble(fila.getOrDefault("Ganancia obtenida", "0"));
            double costo = ParseUtils.toDouble(fila.getOrDefault("Costo Directo", "0"));

            SimpleBooleanProperty check = new SimpleBooleanProperty(true);
            check.addListener((obs, oldVal, newVal) -> recalcular(tabla.getItems(), actualizarValores));

            datos.add(Map.of(
                    "Producto", producto,
                    "Ganancia", ganancia,
                    "Costo", costo,
                    "Check", check
            ));
        }

        tabla.setItems(datos);
        recalcular(datos, actualizarValores);

        VBox.setMargin(tabla, new Insets(10, 0, 0, 0));
        contenedor.getChildren().addAll(titulo, tabla);
    }

    private static void recalcular(List<Map<String, Object>> datos, BiConsumer<Double, Double> actualizar) {
        double sumaGanancia = datos.stream()
                .filter(d -> ((SimpleBooleanProperty) d.get("Check")).get())
                .mapToDouble(d -> (Double) d.get("Ganancia")).sum();

        double sumaCosto = datos.stream()
                .filter(d -> ((SimpleBooleanProperty) d.get("Check")).get())
                .mapToDouble(d -> (Double) d.get("Costo")).sum();

        actualizar.accept(sumaGanancia, sumaCosto);
    }
}
