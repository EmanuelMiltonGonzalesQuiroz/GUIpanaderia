package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Componente simplificado de estadísticas
 * Solo muestra: Precio Total, Costo Total, Diferencia Ganancia
 */
public class EstadisticasSimplificadas {
    
    private Label lblPrecioTotal;
    private Label lblCostoTotal;
    private Label lblDiferenciaGanancia;
    
    public Node crear() {
        VBox panel = ComponentesUI.crearContenedorSeccion(null);
        panel.setPrefWidth(300);
        
        crearLabels();
        agregarLabelsAlPanel(panel);
        inicializarConValoresPorDefecto();
        
        return panel;
    }
    
    public void actualizar(double precioTotal, double costoTotal, double diferenciaGanancia) {
        lblPrecioTotal.setText(String.format("%.2f", precioTotal));
        lblCostoTotal.setText(String.format("%.2f", costoTotal));
        lblDiferenciaGanancia.setText(formatearDiferencia(diferenciaGanancia));
        aplicarColorDiferencia(lblDiferenciaGanancia, diferenciaGanancia);
    }
    
    public void mostrarError() {
        lblPrecioTotal.setText("Error");
        lblCostoTotal.setText("Error");
        lblDiferenciaGanancia.setText("Error");
    }
    
    public void limpiar() {
        inicializarConValoresPorDefecto();
    }
    
    private void crearLabels() {
        lblPrecioTotal = crearLabelEstadistica("0.00");
        lblCostoTotal = crearLabelEstadistica("0.00");
        lblDiferenciaGanancia = crearLabelEstadistica("0.00");
    }
    
    private Label crearLabelEstadistica(String valor) {
        Label label = new Label(valor);
        label.setStyle("-fx-background-color: white; -fx-padding: 8 15; " +
                      "-fx-border-color: #ccc; -fx-border-width: 1; " +
                      "-fx-pref-width: 200; -fx-alignment: CENTER-RIGHT; " +
                      "-fx-font-size: 14px; -fx-font-weight: bold;");
        return label;
    }
    
    private void agregarLabelsAlPanel(VBox panel) {
        panel.getChildren().addAll(
            ComponentesUI.crearEtiquetaTitulo("💰 Precio Total"),
            lblPrecioTotal,
            
            ComponentesUI.crearEtiquetaTitulo("📊 Costo Total"),
            lblCostoTotal,
            
            ComponentesUI.crearEtiquetaTitulo("💸 Diferencia Ganancia"),
            lblDiferenciaGanancia
        );
    }
    
    private String formatearDiferencia(double diferencia) {
        String signo = diferencia >= 0 ? "+" : "";
        return signo + String.format("%.2f", diferencia);
    }
    
    private void aplicarColorDiferencia(Label label, double diferencia) {
        String baseStyle = "-fx-background-color: white; -fx-padding: 8 15; " +
                          "-fx-border-color: #ccc; -fx-border-width: 1; " +
                          "-fx-pref-width: 200; -fx-alignment: CENTER-RIGHT; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; ";
        
        if (diferencia > 0) {
            // Verde para ganancias
            label.setStyle(baseStyle + "-fx-text-fill: #4CAF50;");
        } else if (diferencia < 0) {
            // Rojo para pérdidas
            label.setStyle(baseStyle + "-fx-text-fill: #F44336;");
        } else {
            // Negro para sin ganancia ni pérdida
            label.setStyle(baseStyle + "-fx-text-fill: black;");
        }
    }
    
    private void inicializarConValoresPorDefecto() {
        lblPrecioTotal.setText("0.00");
        lblCostoTotal.setText("0.00");
        lblDiferenciaGanancia.setText("0.00");
        
        // Resetear estilo
        String estiloBase = "-fx-background-color: white; -fx-padding: 8 15; " +
                           "-fx-border-color: #ccc; -fx-border-width: 1; " +
                           "-fx-pref-width: 200; -fx-alignment: CENTER-RIGHT; " +
                           "-fx-font-size: 14px; -fx-font-weight: bold;";
        
        lblDiferenciaGanancia.setStyle(estiloBase);
    }
}