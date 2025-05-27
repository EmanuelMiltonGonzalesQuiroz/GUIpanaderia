package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.ConversorMezclaUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.function.Supplier;

public class FormularioNuevaReceta {

    private final TextField campoCantidad = new TextField();
    private final TextField campoMezcla = new TextField();
    private final TextField campoPrecioUnidad = new TextField();
    private final TextField campoPrecioTotal = new TextField();
    private final TextField campoCostoDirecto = new TextField("0.00");
    private final TextField campoCostoUnitario = new TextField("0.0000");
    private final CheckBox checkGuardarReceta = new CheckBox("Guardar Receta");

    private boolean actualizando = false;

    private GridPane grid;
    private Label campoNombreProducto;
    private Label campoVersion;
    private Label campoRendimiento;

    private String codigoRecetaActual;
    private Runnable onCambioMezclas;

    public Node crear(String nombreProducto, String version, String rendimiento) {
        if (grid == null) inicializarGrid();

        campoNombreProducto.setText(nombreProducto);
        campoVersion.setText(version != null ? version : "-");
        campoRendimiento.setText(rendimiento != null ? rendimiento : "-");

        return grid;
    }

    public void setCodigoReceta(String codigo) {
        this.codigoRecetaActual = codigo;
    }

    public String getCodigoRecetaActual() {
        return codigoRecetaActual;
    }

    public boolean isGuardarReceta() {
        return checkGuardarReceta.isSelected();
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

        campoNombreProducto.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        campoVersion.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        campoRendimiento.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        checkGuardarReceta.setStyle("-fx-font-size: 18px;  -fx-font-weight: bold;");

        restringirSoloNumeros(campoCantidad);
        restringirSoloNumeros(campoMezcla);
        restringirSoloNumeros(campoPrecioUnidad);
        restringirSoloNumeros(campoPrecioTotal);
        restringirSoloNumeros(campoCostoDirecto);
        restringirSoloNumeros(campoCostoUnitario);

        grid.add(crearEtiqueta("Producto:"), 0, 0);
        grid.add(campoNombreProducto, 1, 0);

        grid.add(crearEtiqueta("Versión:"), 0, 1);
        grid.add(campoVersion, 1, 1);

        grid.add(crearEtiqueta("Rendimiento:"), 0, 2);
        grid.add(campoRendimiento, 1, 2);

        grid.add(crearEtiqueta("Mezclas usadas:"), 0, 3);
        grid.add(campoMezcla, 1, 3);

        grid.add(crearEtiqueta("Cantidad producida:"), 0, 4);
        grid.add(campoCantidad, 1, 4);

        grid.add(crearEtiqueta("Precio por unidad:"), 0, 5);
        grid.add(campoPrecioUnidad, 1, 5);

        grid.add(crearEtiqueta("Precio total:"), 0, 6);
        grid.add(campoPrecioTotal, 1, 6);

        grid.add(crearEtiqueta("Costo directo:"), 0, 7);
        grid.add(campoCostoDirecto, 1, 7);

        grid.add(crearEtiqueta("Costo/U:"), 0, 8);
        grid.add(campoCostoUnitario, 1, 8);

        grid.add(checkGuardarReceta, 1, 9);

        campoCantidad.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;

            double cantidad = parseDouble(n);
            if (cantidad > 0 && codigoRecetaActual != null) {
                double mezclas = ConversorMezclaUtils.calcularMezclasDesdeProduccion((int) cantidad, codigoRecetaActual);
                campoMezcla.setText(String.format("%.2f", mezclas));
                if (onCambioMezclas != null) onCambioMezclas.run();
            }

            recalcularDesdeUnidad();
            actualizando = false;
        });

        campoMezcla.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;

            double mezcla = parseDouble(n);
            if (mezcla > 0 && codigoRecetaActual != null) {
                int cantidad = ConversorMezclaUtils.calcularProduccionDesdeMezclas(mezcla, codigoRecetaActual);
                campoCantidad.setText(String.valueOf(cantidad));
                if (onCambioMezclas != null) onCambioMezclas.run();
            }

            actualizando = false;
        });

        campoPrecioUnidad.textProperty().addListener((obs, o, n) -> recalcularDesdeUnidad());
        campoPrecioTotal.textProperty().addListener((obs, o, n) -> recalcularDesdeTotal());
    }

    private void recalcularDesdeUnidad() {
        if (actualizando) return;
        actualizando = true;
        double cantidad = parseDouble(campoCantidad.getText());
        double precioUnidad = parseDouble(campoPrecioUnidad.getText());
        campoPrecioTotal.setText(cantidad <= 0 ? "0.00" : String.format("%.2f", cantidad * precioUnidad));
        actualizando = false;
    }

    private void recalcularDesdeTotal() {
        if (actualizando) return;
        actualizando = true;
        double cantidad = parseDouble(campoCantidad.getText());
        double precioTotal = parseDouble(campoPrecioTotal.getText());
        campoPrecioUnidad.setText(cantidad <= 0 ? "0.00" : String.format("%.2f", precioTotal / cantidad));
        actualizando = false;
    }

    public void setOnCambioMezclas(Runnable r) {
        this.onCambioMezclas = r;
    }

    public String getMezclas() {
        return campoMezcla.getText().trim();
    }

    public void setMezclas(String valor) {
        if (valor != null) campoMezcla.setText(valor);
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

    public String getCostoDirecto() {
        return campoCostoDirecto.getText().trim();
    }

    public String getCostoUnitario() {
        return campoCostoUnitario.getText().trim();
    }

    public void setCantidad(String valor) {
        campoCantidad.setText(valor);
    }

    public void setPrecioUnitario(String valor) {
        campoPrecioUnidad.setText(valor);
    }

    public void setPrecioTotal(String valor) {
        campoPrecioTotal.setText(valor);
    }

    public void setCostoDirecto(String valor) {
        campoCostoDirecto.setText(valor);
    }

    public void setCostoUnitario(String valor) {
        campoCostoUnitario.setText(valor);
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
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        return lbl;
    }

    private double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void restringirSoloNumeros(TextField campo) {
        campo.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*(\\.\\d{0,4})?")) {
                campo.setText(oldText);
            }
        });
    }
}
