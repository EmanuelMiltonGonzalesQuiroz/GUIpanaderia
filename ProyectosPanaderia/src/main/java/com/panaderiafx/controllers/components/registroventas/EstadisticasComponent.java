package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Componente que muestra estadísticas de ventas
 * Panel de información con totales y métricas
 */
public class EstadisticasComponent {
    
    private final VentasService ventasService;
    private Label lblTotalVenta;
    private Label lblTotalCostos;
    private Label lblTotalBeneficios;
    private Label lblProductoMasVendido;
    
    public EstadisticasComponent(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        VBox panel = ComponentesUI.crearContenedorSeccion(null);
        panel.setPrefWidth(280);
        
        crearCamposEstadisticas();
        agregarCamposAlPanel(panel);
        actualizar();
        
        return panel;
    }
    
    public void actualizar() {
        try {
            EstadisticasVenta stats = ventasService.calcularEstadisticas();
            actualizarLabels(stats);
        } catch (Exception e) {
            System.err.println("Error al actualizar estadísticas: " + e.getMessage());
            mostrarEstadisticasPorDefecto();
        }
    }
    
    private void crearCamposEstadisticas() {
        lblTotalVenta = crearLabelEstadistica("0");
        lblTotalCostos = crearLabelEstadistica("0");
        lblTotalBeneficios = crearLabelEstadistica("0");
        lblProductoMasVendido = crearLabelEstadistica("N/A");
    }
    
    private Label crearLabelEstadistica(String valor) {
        Label label = new Label(valor);
        label.setStyle("-fx-background-color: white; -fx-padding: 5 10; " +
                      "-fx-border-color: #ccc; -fx-border-width: 1; " +
                      "-fx-pref-width: 200;");
        return label;
    }
    
    private void agregarCamposAlPanel(VBox panel) {
        panel.getChildren().addAll(
            ComponentesUI.crearEtiquetaTitulo("Total Venta"), lblTotalVenta,
            ComponentesUI.crearEtiquetaTitulo("Total Costos"), lblTotalCostos,
            ComponentesUI.crearEtiquetaTitulo("Total Beneficios"), lblTotalBeneficios,
            ComponentesUI.crearEtiquetaTitulo("Producto más vendido"), lblProductoMasVendido
        );
    }
    
    private void actualizarLabels(EstadisticasVenta stats) {
        if (stats != null) {
            lblTotalVenta.setText(String.format("%.0f", stats.getTotalVentas()));
            lblTotalCostos.setText(String.format("%.0f", stats.getTotalCostos()));
            lblTotalBeneficios.setText(String.format("%.0f", stats.getTotalBeneficios()));
            lblProductoMasVendido.setText(stats.getProductoMasVendido() != null ? 
                                         stats.getProductoMasVendido() : "N/A");
        } else {
            mostrarEstadisticasPorDefecto();
        }
    }
    
    private void mostrarEstadisticasPorDefecto() {
        lblTotalVenta.setText("0");
        lblTotalCostos.setText("0");
        lblTotalBeneficios.setText("0");
        lblProductoMasVendido.setText("N/A");
    }
}