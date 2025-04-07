package com.panaderiafx.controllers;

import javafx.scene.layout.VBox;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;

import com.panaderiafx.utils.ExcelDataUtils;

import java.util.*;

public class GraficoController {

    public static VBox mostrar(String tabla) {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-padding: 20;");

        List<Map<String, String>> datos = ExcelDataUtils.obtenerTabla(tabla);
        if (datos.isEmpty()) return contenedor;

        Set<String> columnas = datos.get(0).keySet();

        ComboBox<String> cbX = new ComboBox<>(FXCollections.observableArrayList(columnas));
        cbX.setPromptText("Columna X");
        ComboBox<String> cbY = new ComboBox<>(FXCollections.observableArrayList(columnas));
        cbY.setPromptText("Columna Y");

        Button btnGraficar = new Button("Graficar");

        VBox graficoBox = new VBox();

        btnGraficar.setOnAction(e -> {
            graficoBox.getChildren().clear();
            String xSel = cbX.getValue(), ySel = cbY.getValue();
            if (xSel == null || ySel == null) return;

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Gráfico: " + ySel + " vs " + xSel);

            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            datos.forEach(r -> {
                try {
                    String x = r.get(xSel);
                    double y = Double.parseDouble(r.get(ySel));
                    serie.getData().add(new XYChart.Data<>(x, y));
                } catch (Exception ignored) {}
            });

            chart.getData().add(serie);
            graficoBox.getChildren().add(chart);
        });

        contenedor.getChildren().addAll(cbX, cbY, btnGraficar, graficoBox);
        return contenedor;
    }
}
