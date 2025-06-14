package com.panaderiafx.controllers.components.registroventas;

/**
 * Modelo que encapsula estadísticas de ventas
 * Contiene métricas calculadas del negocio
 */
public class EstadisticasVenta {
    
    private double totalVentas;
    private double totalCostos;
    private double totalBeneficios;
    private String productoMasVendido;
    private double cantidadMasVendida;
    private int numeroVentas;
    private double promedioVentaPorTransaccion;
    
    public EstadisticasVenta() {}
    
    // Getters
    public double getTotalVentas() { return totalVentas; }
    public double getTotalCostos() { return totalCostos; }
    public double getTotalBeneficios() { return totalBeneficios; }
    public String getProductoMasVendido() { return productoMasVendido; }
    public double getCantidadMasVendida() { return cantidadMasVendida; }
    public int getNumeroVentas() { return numeroVentas; }
    public double getPromedioVentaPorTransaccion() { return promedioVentaPorTransaccion; }
    
    // Setters
    public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }
    public void setTotalCostos(double totalCostos) { this.totalCostos = totalCostos; }
    public void setTotalBeneficios(double totalBeneficios) { this.totalBeneficios = totalBeneficios; }
    public void setProductoMasVendido(String productoMasVendido) { this.productoMasVendido = productoMasVendido; }
    public void setCantidadMasVendida(double cantidadMasVendida) { this.cantidadMasVendida = cantidadMasVendida; }
    public void setNumeroVentas(int numeroVentas) { this.numeroVentas = numeroVentas; }
    public void setPromedioVentaPorTransaccion(double promedio) { this.promedioVentaPorTransaccion = promedio; }
    
    // Métodos calculados
    public double getMargenBeneficios() {
        return totalVentas > 0 ? (totalBeneficios / totalVentas) * 100 : 0;
    }
    
    public boolean esRentable() {
        return totalBeneficios > 0;
    }
    
    @Override
    public String toString() {
        return String.format("Estadísticas{ventas=%.2f, costos=%.2f, beneficios=%.2f, producto='%s'}",
                totalVentas, totalCostos, totalBeneficios, productoMasVendido);
    }
}