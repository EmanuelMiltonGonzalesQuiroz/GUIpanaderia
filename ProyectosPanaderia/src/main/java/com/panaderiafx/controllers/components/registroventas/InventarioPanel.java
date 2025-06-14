package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.List;
import java.util.Map;

import com.panaderiafx.utils.componentes.ParseUtils;

/**
 * Panel de inventario con cálculo real de cantidad restante
 * Restante = Cantidad Producida - Cantidad Vendida
 */
public class InventarioPanel {
    
    private final VentasService ventasService;
    private TableView<InventarioRow> tablaInventario;
    private TextField txtCodigoProduccion;
    private TextField txtFechaVenta;
    private TextField txtPrecioVenta;
    private TextField txtCantidadVendida;
    private TextField txtCantidadRestante;
    private TextField txtCantidadProducida;
    
    public InventarioPanel(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        HBox contenedor = new HBox(20);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FF8A50;");
        contenedor.setPrefWidth(1400); // Ampliado considerablemente
        
        VBox panelIzquierdo = crearPanelTablaInventario();
        VBox panelDerecho = crearPanelDetalleInventario();
        
        panelIzquierdo.setPrefWidth(800); // Ampliado para la tabla
        panelDerecho.setPrefWidth(500);   // Ampliado para el detalle
        
        contenedor.getChildren().addAll(panelIzquierdo, panelDerecho);
        return contenedor;
    }
    
    private VBox crearPanelTablaInventario() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Inventario de Productos");
        
        tablaInventario = new TableView<>();
        configurarTablaInventario();
        cargarDatosInventario();
        
        // Botón actualizar
        Button btnActualizar = ComponentesUI.crearBotonSecundario("🔄 Actualizar Inventario");
        btnActualizar.setPrefWidth(250);
        btnActualizar.setOnAction(e -> actualizarInventario());
        
        panel.getChildren().addAll(titulo, tablaInventario, btnActualizar);
        return panel;
    }
    
    private void configurarTablaInventario() {
        tablaInventario.setPrefHeight(400);
        tablaInventario.setPrefWidth(580);
        
        // Columnas del inventario
        TableColumn<InventarioRow, String> colCodigo = crearColumna("Código Producción", "codigoProduccion", 140);
        TableColumn<InventarioRow, String> colProducto = crearColumna("Producto", "producto", 140);
        TableColumn<InventarioRow, String> colFecha = crearColumna("Fecha", "fecha", 80);
        TableColumn<InventarioRow, String> colProducida = crearColumna("Cantidad Producida", "cantidadProducida", 120);
        TableColumn<InventarioRow, String> colVendida = crearColumna("Cantidad Vendida", "cantidadVendida", 120);
        TableColumn<InventarioRow, String> colRestante = crearColumna("Cantidad Restante", "cantidadRestante", 120);
        
        tablaInventario.getColumns().addAll(colCodigo, colProducto, colFecha, colProducida, colVendida, colRestante);
        
        // Listener para selección
        tablaInventario.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDetalleInventario(newSelection);
            }
        });
        
        // Estilo
        tablaInventario.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
    }
    
    private TableColumn<InventarioRow, String> crearColumna(String titulo, String propiedad, double ancho) {
        TableColumn<InventarioRow, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        columna.setPrefWidth(ancho);
        
        // Alineación para columnas numéricas
        if (propiedad.contains("cantidad") || propiedad.contains("Cantidad")) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        
        return columna;
    }
    
    private VBox crearPanelDetalleInventario() {
        VBox panel = new VBox(15);
        
        // Etiquetas de sección
        Label lblVenta = ComponentesUI.crearEtiquetaTitulo("Detalle de Inventario");
        Label lblProduccion = ComponentesUI.crearEtiquetaTitulo("Produccion");
        
        // Campos del formulario
        txtCodigoProduccion = ComponentesUI.crearCampoTextoDeshabilitado("PRO0001");
        txtCodigoProduccion.setPrefWidth(350);
        
        VBox seccionFecha = crearSeccionFecha();
        VBox seccionCantidadProducida = crearSeccionCantidadProducida();
        VBox seccionCantidadVendida = crearSeccionCantidadVendida();
        VBox seccionCantidadRestante = crearSeccionCantidadRestante();
        VBox seccionPrecio = crearSeccionPrecio();
        
        panel.getChildren().addAll(
            lblVenta, lblProduccion, txtCodigoProduccion,
            seccionFecha, seccionCantidadProducida, seccionCantidadVendida,
            seccionCantidadRestante, seccionPrecio
        );
        
        return panel;
    }
    
    private VBox crearSeccionFecha() {
        Label lblFecha = ComponentesUI.crearEtiquetaTitulo("Fecha de Producción");
        txtFechaVenta = ComponentesUI.crearCampoTextoDeshabilitado("10-06-25");
        txtFechaVenta.setPrefWidth(350);
        return new VBox(5, lblFecha, txtFechaVenta);
    }
    
    private VBox crearSeccionCantidadProducida() {
        Label lblCantidad = ComponentesUI.crearEtiquetaTitulo("Cantidad Producida");
        txtCantidadProducida = ComponentesUI.crearCampoTextoDeshabilitado("0");
        txtCantidadProducida.setPrefWidth(350);
        return new VBox(5, lblCantidad, txtCantidadProducida);
    }
    
    private VBox crearSeccionCantidadVendida() {
        Label lblCantidad = ComponentesUI.crearEtiquetaTitulo("Cantidad Vendida");
        txtCantidadVendida = ComponentesUI.crearCampoTextoDeshabilitado("0");
        txtCantidadVendida.setPrefWidth(350);
        return new VBox(5, lblCantidad, txtCantidadVendida);
    }
    
    private VBox crearSeccionCantidadRestante() {
        Label lblCantidad = ComponentesUI.crearEtiquetaTitulo("Cantidad Restante");
        txtCantidadRestante = ComponentesUI.crearCampoTextoDeshabilitado("0");
        txtCantidadRestante.setPrefWidth(350);
        
        // Estilo especial para cantidad restante
        txtCantidadRestante.setStyle("-fx-background-color: #E8F5E8; -fx-padding: 5 10; " +
                                   "-fx-border-color: #4CAF50; -fx-border-width: 2; " +
                                   "-fx-font-weight: bold; -fx-font-size: 14px;");
        
        return new VBox(5, lblCantidad, txtCantidadRestante);
    }
    
    private VBox crearSeccionPrecio() {
        Label lblPrecio = ComponentesUI.crearEtiquetaTitulo("Precio de Venta");
        txtPrecioVenta = ComponentesUI.crearCampoTextoDeshabilitado("1.85");
        txtPrecioVenta.setPrefWidth(350);
        return new VBox(5, lblPrecio, txtPrecioVenta);
    }
    
    private void cargarDatosInventario() {
        tablaInventario.getItems().clear();
        
        // Obtener datos de producción
        List<Map<String, String>> producciones = ventasService.obtenerDatosProduccion();
        
        System.out.println("📦 Cargando inventario...");
        for (Map<String, String> produccion : producciones) {
            String codigoProduccion = produccion.getOrDefault("Código Producción", "");
            String producto = produccion.getOrDefault("Producto", "");
            String cantidadProducidaStr = produccion.getOrDefault("Cantidad Producida", "0");
            
            // Calcular total vendido para esta producción específica
            double totalVendido = calcularTotalVendido(codigoProduccion);
            double cantidadProducida = ParseUtils.toDouble(cantidadProducidaStr);
            double restante = Math.max(0, cantidadProducida - totalVendido);
            
            System.out.printf("   %s (%s): Producida=%.0f, Vendida=%.0f, Restante=%.0f%n", 
                            codigoProduccion, producto, cantidadProducida, totalVendido, restante);
            
            // Crear fila de inventario
            InventarioRow row = new InventarioRow(produccion, totalVendido);
            tablaInventario.getItems().add(row);
        }
        
        System.out.println("📦 Inventario cargado con " + tablaInventario.getItems().size() + " productos");
    }
    
    private double calcularTotalVendido(String codigoProduccion) {
        List<VentaModel> ventas = ventasService.obtenerTodasLasVentas();
        
        double total = ventas.stream()
                .filter(venta -> codigoProduccion.equals(venta.getCodigoProduccion()))
                .mapToDouble(VentaModel::getCantidadVendida)
                .sum();
        
        System.out.println("📊 " + codigoProduccion + " - Total vendido calculado: " + total);
        return total;
    }
    
    private void cargarDetalleInventario(InventarioRow fila) {
        txtCodigoProduccion.setText(fila.getCodigoProduccion());
        txtFechaVenta.setText(fila.getFecha());
        txtCantidadProducida.setText(fila.getCantidadProducida());
        txtCantidadVendida.setText(fila.getCantidadVendida());
        txtCantidadRestante.setText(fila.getCantidadRestante());
        txtPrecioVenta.setText(fila.getPrecioVenta());
        
        // Cambiar color según el stock
        double restante = Double.parseDouble(fila.getCantidadRestante());
        if (restante <= 0) {
            // Rojo para sin stock
            txtCantidadRestante.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 5 10; " +
                                       "-fx-border-color: #F44336; -fx-border-width: 2; " +
                                       "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #F44336;");
        } else if (restante < 50) {
            // Amarillo para stock bajo
            txtCantidadRestante.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 5 10; " +
                                       "-fx-border-color: #FF9800; -fx-border-width: 2; " +
                                       "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF9800;");
        } else {
            // Verde para stock normal
            txtCantidadRestante.setStyle("-fx-background-color: #E8F5E8; -fx-padding: 5 10; " +
                                       "-fx-border-color: #4CAF50; -fx-border-width: 2; " +
                                       "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");
        }
    }
    
    private void actualizarInventario() {
        System.out.println("🔄 Actualizando inventario...");
        
        // Recargar datos del Excel
        com.panaderiafx.utils.VerUtils.refrescarExcel();
        
        // Recargar inventario
        cargarDatosInventario();
        
        ComponentesUI.mostrarExito("Inventario actualizado correctamente");
    }
    
    // Clase interna para filas de inventario
    public static class InventarioRow {
        private final String codigoProduccion;
        private final String producto;
        private final String fecha;
        private final String cantidadProducida;
        private final String cantidadVendida;
        private final String cantidadRestante;
        private final String precioVenta;
        
        public InventarioRow(Map<String, String> produccion, double totalVendido) {
            this.codigoProduccion = produccion.getOrDefault("Código Producción", "");
            this.producto = produccion.getOrDefault("Producto", "");
            this.fecha = produccion.getOrDefault("Fecha", "");
            
            double cantidadProd = ParseUtils.toDouble(produccion.getOrDefault("Cantidad Producida", "0"));
            // CORRECCIÓN: Calcular restante correctamente (nunca negativo)
            double restante = Math.max(0, cantidadProd - totalVendido);
            
            this.cantidadProducida = String.format("%.0f", cantidadProd);
            this.cantidadVendida = String.format("%.0f", totalVendido);
            this.cantidadRestante = String.format("%.0f", restante);
            this.precioVenta = produccion.getOrDefault("Precio de Venta por Unidad", "0");
        }
        
        public String getCodigoProduccion() { return codigoProduccion; }
        public String getProducto() { return producto; }
        public String getFecha() { return fecha; }
        public String getCantidadProducida() { return cantidadProducida; }
        public String getCantidadVendida() { return cantidadVendida; }
        public String getCantidadRestante() { return cantidadRestante; }
        public String getPrecioVenta() { return precioVenta; }
    }
}