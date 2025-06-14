package com.panaderiafx.controllers.components.registroventas;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculadora de estadísticas de ventas
 * Procesa listas de ventas y genera métricas
 */
public class EstadisticasCalculator {
    
    public EstadisticasVenta calcular(List<VentaModel> ventas) {
        if (ventas == null || ventas.isEmpty()) {
            return crearEstadisticasVacias();
        }
        
        EstadisticasVenta stats = new EstadisticasVenta();
        
        calcularTotales(ventas, stats);
        calcularProductoMasVendido(ventas, stats);
        calcularPromedios(ventas, stats);
        
        return stats;
    }
    
    private void calcularTotales(List<VentaModel> ventas, EstadisticasVenta stats) {
        double totalVentas = 0;
        double totalCostos = 0;
        double totalBeneficios = 0;
        
        for (VentaModel venta : ventas) {
            totalVentas += venta.getIngresoTotal();
            totalCostos += venta.getCostoTotal();
            totalBeneficios += venta.getBeneficio();
        }
        
        stats.setTotalVentas(totalVentas);
        stats.setTotalCostos(totalCostos);
        stats.setTotalBeneficios(totalBeneficios);
        stats.setNumeroVentas(ventas.size());
    }
    
    private void calcularProductoMasVendido(List<VentaModel> ventas, EstadisticasVenta stats) {
        Map<String, Double> ventasPorProducto = ventas.stream()
                .collect(Collectors.groupingBy(
                    VentaModel::getProducto,
                    Collectors.summingDouble(VentaModel::getCantidadVendida)
                ));
        
        Optional<Map.Entry<String, Double>> productoMax = ventasPorProducto.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());
        
        if (productoMax.isPresent()) {
            stats.setProductoMasVendido(productoMax.get().getKey());
            stats.setCantidadMasVendida(productoMax.get().getValue());
        } else {
            stats.setProductoMasVendido("N/A");
            stats.setCantidadMasVendida(0);
        }
    }
    
    private void calcularPromedios(List<VentaModel> ventas, EstadisticasVenta stats) {
        if (stats.getNumeroVentas() > 0) {
            double promedio = stats.getTotalVentas() / stats.getNumeroVentas();
            stats.setPromedioVentaPorTransaccion(promedio);
        } else {
            stats.setPromedioVentaPorTransaccion(0);
        }
    }
    
    private EstadisticasVenta crearEstadisticasVacias() {
        EstadisticasVenta stats = new EstadisticasVenta();
        stats.setTotalVentas(0);
        stats.setTotalCostos(0);
        stats.setTotalBeneficios(0);
        stats.setProductoMasVendido("N/A");
        stats.setCantidadMasVendida(0);
        stats.setNumeroVentas(0);
        stats.setPromedioVentaPorTransaccion(0);
        return stats;
    }
}