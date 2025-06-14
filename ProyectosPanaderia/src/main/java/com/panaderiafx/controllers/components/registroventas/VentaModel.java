package com.panaderiafx.controllers.components.registroventas;

/**
 * Modelo que representa una venta
 * Encapsula los datos de una transacción de venta
 */
public class VentaModel {
    
    private String codigoVenta;
    private String codigoProduccion;
    private String fecha;
    private String producto;
    private double cantidadVendida;
    private double precioVenta;
    private double costoDirecto;
    private double beneficio;
    private double cantidadRestante;
    
    public VentaModel() {}
    
    public VentaModel(String producto, String fecha, double cantidad, double precio) {
        this.producto = producto;
        this.fecha = fecha;
        this.cantidadVendida = cantidad;
        this.precioVenta = precio;
    }
    
    // Getters
    public String getCodigoVenta() { return codigoVenta; }
    public String getCodigoProduccion() { return codigoProduccion; }
    public String getFecha() { return fecha; }
    public String getProducto() { return producto; }
    public double getCantidadVendida() { return cantidadVendida; }
    public double getPrecioVenta() { return precioVenta; }
    public double getCostoDirecto() { return costoDirecto; }
    public double getBeneficio() { return beneficio; }
    public double getCantidadRestante() { return cantidadRestante; }
    
    // Setters
    public void setCodigoVenta(String codigoVenta) { this.codigoVenta = codigoVenta; }
    public void setCodigoProduccion(String codigoProduccion) { this.codigoProduccion = codigoProduccion; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setProducto(String producto) { this.producto = producto; }
    public void setCantidadVendida(double cantidadVendida) { this.cantidadVendida = cantidadVendida; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    public void setCostoDirecto(double costoDirecto) { this.costoDirecto = costoDirecto; }
    public void setBeneficio(double beneficio) { this.beneficio = beneficio; }
    public void setCantidadRestante(double cantidadRestante) { this.cantidadRestante = cantidadRestante; }
    
    // Métodos de utilidad
    public double getIngresoTotal() {
        return cantidadVendida * precioVenta;
    }
    
    public double getCostoTotal() {
        return cantidadVendida * costoDirecto;
    }
    
    public boolean esRentable() {
        return beneficio > 0;
    }
    
    public double getMargenPorcentaje() {
        return precioVenta > 0 ? ((precioVenta - costoDirecto) / precioVenta) * 100 : 0;
    }
    
    @Override
    public String toString() {
        return String.format("Venta{codigo='%s', producto='%s', cantidad=%.0f, precio=%.2f, beneficio=%.2f}",
                codigoVenta, producto, cantidadVendida, precioVenta, beneficio);
    }
}