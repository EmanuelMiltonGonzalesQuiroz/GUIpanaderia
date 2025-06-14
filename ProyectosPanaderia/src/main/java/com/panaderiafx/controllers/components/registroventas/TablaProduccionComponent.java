package com.panaderiafx.controllers.components.registroventas;

import com.panaderiafx.utils.VerUtils;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Map;

/**
 * Componente para mostrar tabla de producción ampliada
 * Muestra las columnas principales con mayor ancho
 */
public class TablaProduccionComponent {
    
    private final VentasService ventasService;
    private TableView<ProduccionRow> tabla;
    private Runnable onSelectionChange;
    
    public TablaProduccionComponent(VentasService ventasService) {
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
    
    public ProduccionRow getSeleccionado() {
        return tabla.getSelectionModel().getSelectedItem();
    }
    
    public void actualizar() {
        cargarDatos();
    }
    
    private void configurarTabla() {
        tabla = new TableView<>();
        tabla.setPrefHeight(400);
        tabla.setPrefWidth(530); // Ampliado
        
        // Configurar las columnas principales con anchos optimizados
        TableColumn<ProduccionRow, String> colCodigo = crearColumna("Código Producción", "codigoProduccion", 110);
        TableColumn<ProduccionRow, String> colFecha = crearColumna("Fecha", "fecha", 85);
        TableColumn<ProduccionRow, String> colCantidad = crearColumna("Cantidad Producida", "cantidadProducida", 110);
        TableColumn<ProduccionRow, String> colMezcla = crearColumna("Mezcla", "mezcla", 70);
        TableColumn<ProduccionRow, String> colProducto = crearColumna("Producto", "producto", 140);
        
        tabla.getColumns().addAll(colCodigo, colFecha, colCantidad, colMezcla, colProducto);
        
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
    
    private TableColumn<ProduccionRow, String> crearColumna(String titulo, String propiedad, double ancho) {
        TableColumn<ProduccionRow, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        columna.setPrefWidth(ancho);
        columna.setMinWidth(ancho - 10);
        columna.setMaxWidth(ancho + 20);
        
        // Alineación para columnas numéricas
        if (propiedad.equals("cantidadProducida") || propiedad.equals("mezcla")) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        
        return columna;
    }
    
    private void cargarDatos() {
        tabla.getItems().clear();
        
        // Cargar datos de la tabla "Produccion" del Excel
        List<Map<String, String>> producciones = VerUtils.verTabla("Produccion");
        
        for (Map<String, String> produccion : producciones) {
            ProduccionRow row = new ProduccionRow(produccion);
            tabla.getItems().add(row);
        }
        
        System.out.println("📊 Tabla Producción cargada con " + tabla.getItems().size() + " registros");
    }
    
    // Clase interna para la tabla de producción
    public static class ProduccionRow {
        private final String codigoProduccion;
        private final String fecha;
        private final String cantidadProducida;
        private final String mezcla;
        private final String producto;
        
        public ProduccionRow(Map<String, String> produccion) {
            this.codigoProduccion = produccion.getOrDefault("Código Producción", "");
            this.fecha = produccion.getOrDefault("Fecha", "");
            this.cantidadProducida = produccion.getOrDefault("Cantidad Producida", "");
            this.mezcla = produccion.getOrDefault("Mezcla", "");
            this.producto = produccion.getOrDefault("Producto", "");
        }
        
        public String getCodigoProduccion() { return codigoProduccion; }
        public String getFecha() { return fecha; }
        public String getCantidadProducida() { return cantidadProducida; }
        public String getMezcla() { return mezcla; }
        public String getProducto() { return producto; }
    }
}