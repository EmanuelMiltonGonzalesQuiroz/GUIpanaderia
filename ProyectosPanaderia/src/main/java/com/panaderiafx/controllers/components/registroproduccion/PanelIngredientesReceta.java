package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PanelIngredientesReceta {

    public static Node crear(String codigoReceta) {
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(15));
        contenedor.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");

        TableView<IngredienteDetalle> tabla = new TableView<>();

        TableColumn<IngredienteDetalle, String> colCodigo = new TableColumn<>("Ingrediente");
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigo));

        TableColumn<IngredienteDetalle, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cantidad));

        TableColumn<IngredienteDetalle, String> colUnidad = new TableColumn<>("Unidad");
        colUnidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unidad));

        TableColumn<IngredienteDetalle, String> colCosto = new TableColumn<>("Costo");
        colCosto.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().costo));

        TableColumn<IngredienteDetalle, Boolean> colCheck = new TableColumn<>("✓");
        colCheck.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().usar));
        colCheck.setCellFactory(CheckBoxTableCell.forTableColumn(colCheck));

        tabla.getColumns().addAll(colCodigo, colCantidad, colUnidad, colCosto, colCheck);

        List<Map<String, String>> listaIngredientes = VerUtils.verTabla("Ingredientes");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");

        // MANTENER ORDEN DE EXCEL
        List<IngredienteDetalle> detalles = listaIngredientes.stream()
            .map(i -> {
                String cod = i.getOrDefault("Código", "").trim();
                Map<String, String> filaReceta = recetasIngredientes.stream()
                        .filter(r -> cod.equalsIgnoreCase(r.getOrDefault("Ingrediente", "").trim()))
                        .filter(r -> codigoReceta.equalsIgnoreCase(r.getOrDefault("Código receta", "").trim()))
                        .findFirst().orElse(null);

                if (filaReceta == null) return null;

                String cantidad = filaReceta.getOrDefault("Cantidad", "");
                String unidad = filaReceta.getOrDefault("Unidades", "");
                String costo = i.getOrDefault("Precio Local", "0");

                return new IngredienteDetalle(cod, cantidad, unidad, costo, true);
            })
            .filter(i -> i != null)
            .collect(Collectors.toList());

        tabla.getItems().setAll(detalles);

        // Total de costos
        double total = detalles.stream().mapToDouble(i -> {
            try {
                return Double.parseDouble(i.costo);
            } catch (Exception e) {
                return 0;
            }
        }).sum();

        HBox resumen = new HBox(10);
        resumen.setPadding(new Insets(10));
        resumen.setStyle("-fx-background-color: #FFCC80; -fx-background-radius: 5;");
        Label lbl = new Label("COSTO TOTAL:");
        TextField campo = new TextField(String.format("%.2f", total));
        campo.setEditable(false);
        resumen.getChildren().addAll(lbl, campo);

        contenedor.getChildren().addAll(tabla, resumen);
        return contenedor;
    }

    public static class IngredienteDetalle {
        String codigo, cantidad, unidad, costo;
        boolean usar;

        public IngredienteDetalle(String codigo, String cantidad, String unidad, String costo, boolean usar) {
            this.codigo = codigo;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.costo = costo;
            this.usar = usar;
        }
    }
}
