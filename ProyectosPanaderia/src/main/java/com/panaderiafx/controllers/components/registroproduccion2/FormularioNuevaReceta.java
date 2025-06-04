package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.ConversorMezclaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Map;
import java.util.function.Supplier;

public class FormularioNuevaReceta {

    private final TextField campoCantidad = new TextField();
    private final TextField campoMezcla = new TextField();
    private final TextField campoPrecioUnidad = new TextField();
    private final TextField campoPrecioTotal = new TextField();
    private final TextField campoCostoDirecto = new TextField("0.00");
    private final TextField campoCostoUnitario = new TextField("0.0000");
    private final TextField campoPrecioRegistrado = new TextField();
    private final ComboBox<String> comboSeleccionPrecio = new ComboBox<>();
    private final CheckBox checkGuardarReceta = new CheckBox("Guardar Receta");

    private boolean actualizando = false;
    private GridPane grid;
    private Label campoNombreProducto, campoVersion, campoRendimiento;

    private String codigoRecetaActual;
    private Map<String, String> filaReceta;
    private Runnable onCambioMezclas;

    public Node crear(String nombreProducto, String version, String rendimiento) {
        if (grid == null) inicializarGrid();
        campoNombreProducto.setText(nombreProducto);
        campoVersion.setText(version != null ? version : "-");
        campoRendimiento.setText(rendimiento != null ? rendimiento : "-");
        return grid;
    }

    public void setCodigoReceta(String codigo, Map<String, String> fila) {
        this.codigoRecetaActual = codigo;
        this.filaReceta = fila;
        actualizarPrecioRegistrado();
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

        for (TextField tf : new TextField[]{campoCantidad, campoMezcla, campoPrecioUnidad, campoPrecioTotal, campoCostoDirecto, campoCostoUnitario, campoPrecioRegistrado})
            restringirSoloNumeros(tf);

        comboSeleccionPrecio.getItems().addAll("Precio por Mayor", "Precio Publics Supermarket");
        comboSeleccionPrecio.getSelectionModel().select("Precio por Mayor");
        comboSeleccionPrecio.setOnAction(e -> actualizarPrecioRegistrado());

        String[] etiquetas = {
            "Producto:", "Versión:", "Rendimiento:", "Selección de precios:", "Precio registrado:",
            "Mezclas usadas:", "Cantidad producida:", "Precio por unidad:", "Precio total:",
            "Costo directo:", "Costo/U:"
        };
        Node[] campos = {
            campoNombreProducto, campoVersion, campoRendimiento, comboSeleccionPrecio, campoPrecioRegistrado,
            campoMezcla, campoCantidad, campoPrecioUnidad, campoPrecioTotal,
            campoCostoDirecto, campoCostoUnitario
        };

        for (int i = 0; i < etiquetas.length; i++) {
            grid.add(crearEtiqueta(etiquetas[i]), 0, i);
            grid.add(campos[i], 1, i);
        }
        grid.add(checkGuardarReceta, 1, etiquetas.length);

        campoCantidad.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;
            double cantidad = ParseUtils.safeParseDouble(n);
            if (cantidad > 0 && codigoRecetaActual != null) {
                double mezclas = ConversorMezclaUtils.calcularMezclasDesdeProduccion((int) cantidad, codigoRecetaActual);
                campoMezcla.setText(String.format("%.2f", mezclas));
                if (onCambioMezclas != null) onCambioMezclas.run();
            }
            recalcularDesdePrecioRegistrado();
            actualizando = false;
        });

        campoMezcla.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;
            double mezcla = ParseUtils.safeParseDouble(n);
            if (mezcla > 0 && codigoRecetaActual != null) {
                int cantidad = ConversorMezclaUtils.calcularProduccionDesdeMezclas(mezcla, codigoRecetaActual);
                campoCantidad.setText(String.valueOf(cantidad));
                if (onCambioMezclas != null) onCambioMezclas.run();
            }
            recalcularDesdePrecioRegistrado();
            actualizando = false;
        });

        campoPrecioRegistrado.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;
            recalcularDesdePrecioRegistrado();
            actualizando = false;
        });

        campoPrecioUnidad.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;
            double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
            double precioUnidad = ParseUtils.safeParseDouble(n);
            double precioTotal = cantidad * precioUnidad;
            campoPrecioTotal.setText(String.format("%.2f", precioTotal));
            sincronizarDatos();
            actualizando = false;
        });

        campoPrecioTotal.textProperty().addListener((obs, o, n) -> {
            if (actualizando) return;
            actualizando = true;
            double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
            double precioTotal = ParseUtils.safeParseDouble(n);
            if (cantidad > 0) {
                double precioUnidad = precioTotal / cantidad;
                campoPrecioUnidad.setText(String.format("%.4f", precioUnidad)); // 🔧 CORREGIDO A 4 DECIMALES
            }
            sincronizarDatos();
            actualizando = false;
        });

        campoCostoDirecto.textProperty().addListener((obs, o, n) -> sincronizarDatos());
        campoCostoUnitario.textProperty().addListener((obs, o, n) -> sincronizarDatos());
    }

    private void actualizarPrecioRegistrado() {
        if (filaReceta == null) return;
        String tipoPrecio = comboSeleccionPrecio.getValue();
        if (tipoPrecio == null || tipoPrecio.isBlank()) tipoPrecio = "Precio por Mayor";
        String valorPrecio = filaReceta.getOrDefault(tipoPrecio, "").trim();
        campoPrecioRegistrado.setText(valorPrecio);
        recalcularDesdePrecioRegistrado();
    }

    private void recalcularDesdePrecioRegistrado() {
        double precioCrudo = ParseUtils.safeParseDouble(campoPrecioRegistrado.getText());
        String unidadesStr = filaReceta != null ? filaReceta.getOrDefault("Unidades por Molde", "").trim() : "";
        String moldesStr = filaReceta != null ? filaReceta.getOrDefault("Molde/Paquete", "").trim() : "";

        boolean esPrecioPorMolde = ParseUtils.esNumero(unidadesStr) && ParseUtils.esNumero(moldesStr);
        double precioUnidad = esPrecioPorMolde ? precioCrudo / ParseUtils.safeParseDouble(unidadesStr) : precioCrudo;

        double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
        double precioTotal = cantidad * precioUnidad;

        campoPrecioUnidad.setText(String.format("%.4f", precioUnidad)); // 🔧 CORREGIDO A 4 DECIMALES
        campoPrecioTotal.setText(String.format("%.2f", precioTotal));
        sincronizarDatos();
    }

    private void sincronizarDatos() {
        if (filaReceta == null) return;
        // 🔧 MEJORA: Usar la clave correcta que espera GuardarProduccionUtils
        filaReceta.put("Precio de Venta por Unidad", campoPrecioUnidad.getText().trim());
        filaReceta.put("Precio Total", campoPrecioTotal.getText().trim());
        filaReceta.put("Precio registrado", campoPrecioRegistrado.getText().trim());
        filaReceta.put("Cantidad producida", campoCantidad.getText().trim());
        filaReceta.put("Mezcla", campoMezcla.getText().trim());
        filaReceta.put("Costo directo", campoCostoDirecto.getText().trim());
        filaReceta.put("Costo/U", campoCostoUnitario.getText().trim());
        
        // 🔧 DEPURACIÓN: Imprimir valores para verificar
        System.out.println("🔧 Sincronizando datos:");
        System.out.println("   Precio de Venta por Unidad: " + filaReceta.get("Precio de Venta por Unidad"));
        System.out.println("   Precio Total: " + filaReceta.get("Precio Total"));
        System.out.println("   Cantidad producida: " + filaReceta.get("Cantidad producida"));
    }

    // Públicos
    public String getCantidad() { return campoCantidad.getText().trim(); }
    public String getMezclas() { return campoMezcla.getText().trim(); }
    public String getPrecioUnitario() { return campoPrecioUnidad.getText().trim(); }
    public String getPrecioTotal() { return campoPrecioTotal.getText().trim(); }
    public String getCostoDirecto() { return campoCostoDirecto.getText().trim(); }
    public String getCostoUnitario() { return campoCostoUnitario.getText().trim(); }
    public String getPrecioRegistrado() { return campoPrecioRegistrado.getText().trim(); }
    public boolean isGuardarReceta() { return checkGuardarReceta.isSelected(); }
    public String getCodigoRecetaActual() { return codigoRecetaActual; }
    public Supplier<String> getCantidadSupplier() { return this::getCantidad; }

    public void setCantidad(String v) { 
        campoCantidad.setText(v); 
        sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
    }
    public void setPrecioUnitario(String v) { 
        campoPrecioUnidad.setText(v); 
        sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
    }
    public void setPrecioTotal(String v) { 
        campoPrecioTotal.setText(v); 
        sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
    }
    public void setCostoDirecto(String v) { 
        campoCostoDirecto.setText(v); 
        sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
    }
    public void setCostoUnitario(String v) { 
        campoCostoUnitario.setText(v); 
        sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
    }
    public void setMezclas(String v) { 
        if (v != null) {
            campoMezcla.setText(v); 
            sincronizarDatos(); // 🔧 MEJORA: Sincronizar después de cambios programáticos
        }
    }
    public void setOnCambioMezclas(Runnable r) { this.onCambioMezclas = r; }
    public Node getNode() { return grid; }

    private Label crearEtiqueta(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-background-color: #FFC107; -fx-font-weight: bold; -fx-font-size: 20px; -fx-padding: 5 10;");
        return lbl;
    }

    private Label crearValorLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        return lbl;
    }

    private void restringirSoloNumeros(TextField campo) {
        campo.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*(\\.|,)?\\d{0,4}")) campo.setText(oldText);
        });
    }
}