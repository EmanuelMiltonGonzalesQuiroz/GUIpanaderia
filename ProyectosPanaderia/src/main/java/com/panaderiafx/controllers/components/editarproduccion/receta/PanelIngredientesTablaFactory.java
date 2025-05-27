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
                                  Map<String, String> produccion,
                                  String codigoProduccion,
                                  BiConsumer<String, Double> actualizarCostoEnTabla) {

        ObservableList<Map<String, String>> observables = FXCollections.observableArrayList(datos);

        TextField campoTotal = PanelIngredientesEstiloUtils.crearCampoResumen("0.00");
        TextField campoUnitario = PanelIngredientesEstiloUtils.crearCampoResumen("0.0000");

        TableView<Map<String, String>> tabla = crearTableView(observables, campoTotal, campoUnitario,
                codigoProduccion, produccion, actualizarCostoEnTabla);

        VBox totales = crearPanelTotales(campoTotal, campoUnitario);
        ColumnasIngredientesFactory.actualizarTotales(observables, campoTotal, campoUnitario,
                codigoProduccion, produccion, actualizarCostoEnTabla);

        return new VBox(10, tabla, totales);
    }

    private static TableView<Map<String, String>> crearTableView(ObservableList<Map<String, String>> observables,
                                                                 TextField campoTotal, TextField campoUnitario,
                                                                 String codigoProduccion, Map<String, String> produccion,
                                                                 BiConsumer<String, Double> actualizarCostoEnTabla) {

        TableView<Map<String, String>> tabla = new TableView<>(observables);
        tabla.setPrefHeight(500);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabla.getColumns().addAll(
                ColumnasIngredientesFactory.columnaIngrediente(),
                ColumnasIngredientesFactory.columnaEditableCantidad(observables, campoTotal, campoUnitario,
                        codigoProduccion, produccion, actualizarCostoEnTabla),
                ColumnasIngredientesFactory.columnaSimple("Unidad", "Unidades"),
                ColumnasIngredientesFactory.columnaSimple("Costo", "Costo"),
                ColumnasIngredientesFactory.columnaCheck(observables, campoTotal, campoUnitario,
                        codigoProduccion, produccion, actualizarCostoEnTabla)
        );

        return tabla;
    }

    private static VBox crearPanelTotales(TextField campoTotal, TextField campoUnitario) {
        VBox totales = new VBox(5,
                new HBox(10, new Label("Costo Total:"), campoTotal),
                new HBox(10, new Label("Costo x Unidad:"), campoUnitario)
        );
        totales.setPadding(new Insets(10));
        totales.setStyle("-fx-background-color: #FFCC80;");
        return totales;
    }
}
