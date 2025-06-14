package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

/**
 * Componente para mostrar tabla de ventas ampliada
 * Muestra todos los campos de la tabla Ventas con mayor ancho
 */
public class TablaVentasComponent {
    
    private final VentasService ventasService;
    private TableView<VentaRow> tabla;
    private Runnable onSelectionChange;
    
    public TablaVentasComponent(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        configurarTabla();
        cargarDatos();
        return tabla;
    }
    
    public void setOnSelectionChange(Runnable callback) {
        this.onSelectionChange = callback;
    }
    
    public VentaRow getSeleccionado() {
        return tabla.getSelectionModel().getSelectedItem();
    }
    
    public void actualizar() {
        cargarDatos();
    }
    
    private void configurarTabla() {
        tabla = new TableView<>();
        tabla.setPrefHeight(400);
        tabla.setPrefWidth(680); // Ampliado
        
        // Configurar todas las columnas de la tabla Ventas con anchos optimizados
        TableColumn<VentaRow, String> colCodigo = crearColumna("Código Venta", "codigoVenta", 95);
        TableColumn<VentaRow, String> colCodigoProdu = crearColumna("Código Producción", "codigoProduccion", 110);
        TableColumn<VentaRow, String> colFecha = crearColumna("Fecha", "fecha", 80);
        TableColumn<VentaRow, String> colCantidad = crearColumna("Cantidad Vendida", "cantidadVendida", 100);
        TableColumn<VentaRow, String> colPrecio = crearColumna("Precio Venta/U", "precioVenta", 95);
        TableColumn<VentaRow, String> colCosto = crearColumna("Costo Directo/U", "costoDirecto", 100);
        TableColumn<VentaRow, String> colBeneficio = crearColumna("Beneficio", "beneficio", 80);
        
        tabla.getColumns().addAll(colCodigo, colCodigoProdu, colFecha, colCantidad, 
                                 colPrecio, colCosto, colBeneficio);
        
        // Política de redimensionamiento
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Configurar selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && onSelectionChange != null) {
                onSelectionChange.run();
            }
        });
        
        // Estilo
        tabla.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
    }
    
    private TableColumn<VentaRow, String> crearColumna(String titulo, String propiedad, double ancho) {
        TableColumn<VentaRow, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        columna.setPrefWidth(ancho);
        columna.setMinWidth(ancho - 10);
        columna.setMaxWidth(ancho + 20);
        
        // Alineación para columnas numéricas
        if (propiedad.equals("cantidadVendida") || propiedad.equals("precioVenta") || 
            propiedad.equals("costoDirecto") || propiedad.equals("beneficio")) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        
        return columna;
    }
    
    private void cargarDatos() {
        tabla.getItems().clear();
        List<VentaModel> ventas = ventasService.obtenerTodasLasVentas();
        
        for (VentaModel venta : ventas) {
            VentaRow row = new VentaRow(venta);
            tabla.getItems().add(row);
        }
        
        System.out.println("📊 Tabla Ventas cargada con " + tabla.getItems().size() + " registros");
    }
    
    // Clase interna para la tabla completa
    public static class VentaRow {
        private final String codigoVenta;
        private final String codigoProduccion;
        private final String fecha;
        private final String cantidadVendida;
        private final String precioVenta;
        private final String costoDirecto;
        private final String beneficio;
        private final VentaModel ventaOriginal;
        
        public VentaRow(VentaModel venta) {
            this.codigoVenta = venta.getCodigoVenta() != null ? venta.getCodigoVenta() : "";
            this.codigoProduccion = venta.getCodigoProduccion() != null ? venta.getCodigoProduccion() : "";
            this.fecha = venta.getFecha() != null ? venta.getFecha() : "";
            this.cantidadVendida = String.format("%.0f", venta.getCantidadVendida());
            this.precioVenta = String.format("%.4f", venta.getPrecioVenta());
            this.costoDirecto = String.format("%.4f", venta.getCostoDirecto());
            this.beneficio = String.format("%.2f", venta.getBeneficio());
            this.ventaOriginal = venta;
        }
        
        public String getCodigoVenta() { return codigoVenta; }
        public String getCodigoProduccion() { return codigoProduccion; }
        public String getFecha() { return fecha; }
        public String getCantidadVendida() { return cantidadVendida; }
        public String getPrecioVenta() { return precioVenta; }
        public String getCostoDirecto() { return costoDirecto; }
        public String getBeneficio() { return beneficio; }
        public VentaModel getVentaOriginal() { return ventaOriginal; }
    }
}