package com.panaderiafx.controllers.components.editarproduccion.receta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PanelIngredientesTablaFactory {

    public static VBox crearTabla(List<Map<String, String>> datos,
                                  Map<String, String> mapaNombre,
                                  Map<String, String> prod,
                                  String codProduccion,
                                  BiConsumer<String, Double> actualizarCostoEnTabla) {

        ObservableList<Map<String, String>> observables = FXCollections.observableArrayList(datos);

        TextField campoTotal = PanelIngredientesEstiloUtils.crearCampoResumen("0.00");
        TextField campoUnitario = PanelIngredientesEstiloUtils.crearCampoResumen("0.0000");

        TableView<Map<String, String>> tabla = new TableView<>(observables);
        tabla.setPrefHeight(500);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabla.getColumns().addAll(
                ColumnasIngredientesFactory.columnaIngrediente(mapaNombre),
                ColumnasIngredientesFactory.columnaEditableCantidad(observables, campoTotal, campoUnitario, codProduccion, prod, actualizarCostoEnTabla),
                ColumnasIngredientesFactory.columnaSimple("Unidad", "Unidades"),
                ColumnasIngredientesFactory.columnaSimple("Costo", "Costo"),
                ColumnasIngredientesFactory.columnaCheck(observables, campoTotal, campoUnitario, codProduccion, prod, actualizarCostoEnTabla)
        );

        VBox totales = new VBox(5,
                new HBox(10, new Label("Costo Total:"), campoTotal),
                new HBox(10, new Label("Costo x Unidad:"), campoUnitario)
        );
        totales.setPadding(new Insets(10));
        totales.setStyle("-fx-background-color: #FFCC80;");

        ColumnasIngredientesFactory.actualizarTotales(observables, campoTotal, campoUnitario, codProduccion, prod, actualizarCostoEnTabla);

        return new VBox(10, tabla, totales);
    }
}
