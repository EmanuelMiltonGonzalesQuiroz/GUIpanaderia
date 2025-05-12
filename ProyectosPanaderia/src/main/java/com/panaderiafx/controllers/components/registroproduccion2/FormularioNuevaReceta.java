package com.panaderiafx.controllers.components.registroproduccion2;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.function.Supplier;

public class FormularioNuevaReceta {

    private final TextField campoCantidad = new TextField();
    private final TextField campoPrecioUnidad = new TextField();
    private final TextField campoPrecioTotal = new TextField();

    private String cacheCantidad = "";
    private String cachePrecioUnidad = "";

    private GridPane grid;
    private Label campoNombreProducto;
    private Label campoVersion;
    private Label campoRendimiento;

    public Node crear(String nombreProducto, String version, String rendimiento) {
        if (grid == null) inicializarGrid();

        campoNombreProducto.setText(nombreProducto);
        campoVersion.setText(version != null ? version : "-");
        campoRendimiento.setText(rendimiento != null ? rendimiento : "-");
        return grid;
    }

    private void inicializarGrid() {
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #F36C00; -fx-padding: 20; -fx-background-radius: 10;");

        campoNombreProducto = crearValorLabel("-");
        campoVersion = crearValorLabel("-");
        campoRendimiento = crearValorLabel("-");
        campoPrecioTotal.setEditable(false);

        grid.add(crearEtiqueta("Producto:"), 0, 0);
        grid.add(campoNombreProducto, 1, 0);

        grid.add(crearEtiqueta("Versión:"), 0, 1);
        grid.add(campoVersion, 1, 1);

        grid.add(crearEtiqueta("Rendimiento:"), 0, 2);
        grid.add(campoRendimiento, 1, 2);

        grid.add(crearEtiqueta("Cantidad producida:"), 0, 3);
        grid.add(campoCantidad, 1, 3);

        grid.add(crearEtiqueta("Precio por unidad:"), 0, 4);
        grid.add(campoPrecioUnidad, 1, 4);

        grid.add(crearEtiqueta("Precio total:"), 0, 5);
        grid.add(campoPrecioTotal, 1, 5);

        campoCantidad.textProperty().addListener((obs, o, n) -> recalcular());
        campoPrecioUnidad.textProperty().addListener((obs, o, n) -> recalcular());
    }

    private void recalcular() {
        String nuevaCantidad = campoCantidad.getText().trim();
        String nuevoPrecio = campoPrecioUnidad.getText().trim();

        if (nuevaCantidad.equals(cacheCantidad) && nuevoPrecio.equals(cachePrecioUnidad)) return;

        cacheCantidad = nuevaCantidad;
        cachePrecioUnidad = nuevoPrecio;

        double cant = parseDouble(nuevaCantidad);
        double unit = parseDouble(nuevoPrecio);
        campoPrecioTotal.setText(String.format("%.2f", cant * unit));
    }

    public String getCantidad() {
        return campoCantidad.getText().trim();
    }

    public String getPrecioUnitario() {
        return campoPrecioUnidad.getText().trim();
    }

    public String getPrecioTotal() {
        return campoPrecioTotal.getText().trim();
    }

    public void setCantidad(String valor) {
        campoCantidad.setText(valor);
        cacheCantidad = "";
    }

    public void setPrecioUnitario(String valor) {
        campoPrecioUnidad.setText(valor);
        cachePrecioUnidad = "";
    }

    public void setPrecioTotal(String valor) {
        campoPrecioTotal.setText(valor);
    }

    public Supplier<String> getCantidadSupplier() {
        return this::getCantidad;
    }

    public Node getNode() {
        return grid;
    }

    private Label crearEtiqueta(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle(
            "-fx-background-color: #FFC107;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 20px;" +
            "-fx-padding: 5 10;"
        );
        return lbl;
    }

    private Label crearValorLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return lbl;
    }

    private double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
