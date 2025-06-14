package com.panaderiafx.controllers.components.registroventas;

import com.panaderiafx.utils.*;
import com.panaderiafx.utils.componentes.ParseUtils;
import java.util.*;

/**
 * Servicio de ventas con validación de stock y generador automático de códigos
 */
public class VentasService {
    
    private static final String TABLA_VENTAS = "Ventas";
    private static final String TABLA_PRODUCCION = "Produccion";
    
    public List<VentaModel> obtenerTodasLasVentas() {
        List<Map<String, String>> datos = VerUtils.verTabla(TABLA_VENTAS);
        return datos.stream()
                .map(this::mapearAVentaModel)
                .toList();
    }
    
    public List<String> obtenerProductosDisponibles() {
        List<String> productosProduccion = VerUtils.verColumna(TABLA_PRODUCCION, "Producto");
        List<String> productosOriginales = VerUtils.verColumna("Recetas", "Producto");
        
        Set<String> todosProductos = new HashSet<>();
        todosProductos.addAll(productosProduccion);
        todosProductos.addAll(productosOriginales);
        
        return todosProductos.stream()
                .filter(p -> !p.isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    public List<Map<String, String>> obtenerDatosProduccion() {
        return VerUtils.verTabla(TABLA_PRODUCCION);
    }
    
    /**
     * Obtiene la cantidad máxima que se puede vender de un producto específico
     * @param codigoProduccion Código de la producción
     * @return Cantidad disponible (Producida - Ya Vendida)
     */
    public double obtenerCantidadDisponible(String codigoProduccion) {
        // Obtener cantidad producida
        String cantidadProducidaStr = VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Código Producción", codigoProduccion, "Cantidad Producida");
        double cantidadProducida = ParseUtils.toDouble(cantidadProducidaStr);
        
        // Calcular total ya vendido
        double totalVendido = obtenerTodasLasVentas().stream()
                .filter(venta -> codigoProduccion.equals(venta.getCodigoProduccion()))
                .mapToDouble(VentaModel::getCantidadVendida)
                .sum();
        
        return Math.max(0, cantidadProducida - totalVendido);
    }
    
    /**
     * Obtiene la cantidad máxima que se puede vender excluyendo una venta específica (para edición)
     * @param codigoProduccion Código de la producción
     * @param codigoVentaExcluir Código de venta a excluir del cálculo
     * @return Cantidad disponible para edición
     */
    public double obtenerCantidadDisponibleParaEdicion(String codigoProduccion, String codigoVentaExcluir) {
        // Obtener cantidad producida
        String cantidadProducidaStr = VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Código Producción", codigoProduccion, "Cantidad Producida");
        double cantidadProducida = ParseUtils.toDouble(cantidadProducidaStr);
        
        // Calcular total ya vendido excluyendo la venta actual
        double totalVendido = obtenerTodasLasVentas().stream()
                .filter(venta -> codigoProduccion.equals(venta.getCodigoProduccion()))
                .filter(venta -> !codigoVentaExcluir.equals(venta.getCodigoVenta()))
                .mapToDouble(VentaModel::getCantidadVendida)
                .sum();
        
        return Math.max(0, cantidadProducida - totalVendido);
    }
    
    /**
     * Valida si una cantidad es válida para vender
     * @param codigoProduccion Código de la producción
     * @param cantidadVender Cantidad que se quiere vender
     * @return true si es válida, false si excede el stock
     */
    public boolean validarCantidadVenta(String codigoProduccion, double cantidadVender) {
        double disponible = obtenerCantidadDisponible(codigoProduccion);
        return cantidadVender <= disponible;
    }
    
    /**
     * Valida si una cantidad es válida para editar una venta
     * @param codigoProduccion Código de la producción
     * @param cantidadVender Cantidad que se quiere vender
     * @param codigoVentaExcluir Código de venta a excluir
     * @return true si es válida, false si excede el stock
     */
    public boolean validarCantidadVentaParaEdicion(String codigoProduccion, double cantidadVender, String codigoVentaExcluir) {
        double disponible = obtenerCantidadDisponibleParaEdicion(codigoProduccion, codigoVentaExcluir);
        return cantidadVender <= disponible;
    }
    
    public boolean crearVenta(VentaModel venta) {
        try {
            String codigoProduccion = venta.getCodigoProduccion();
            if (codigoProduccion == null || codigoProduccion.isEmpty()) {
                codigoProduccion = obtenerCodigoProduccion(venta.getProducto());
            }
            
            double costoDirecto = obtenerCostoDirecto(venta.getProducto());
            double beneficio = calcularBeneficio(venta, costoDirecto);
            
            Map<String, String> filaVenta = construirFilaVenta(venta, codigoProduccion, costoDirecto, beneficio);
            return CrearUtils.crearFila(TABLA_VENTAS, filaVenta);
        } catch (Exception e) {
            System.err.println("Error al crear venta: " + e.getMessage());
            return false;
        }
    }
    
    public boolean crearVentaConCodigo(VentaModel venta) {
        try {
            // Validar stock antes de crear
            String codigoProduccion = venta.getCodigoProduccion();
            if (!validarCantidadVenta(codigoProduccion, venta.getCantidadVendida())) {
                double disponible = obtenerCantidadDisponible(codigoProduccion);
                System.err.println("❌ Stock insuficiente. Disponible: " + disponible + ", Solicitado: " + venta.getCantidadVendida());
                return false;
            }
            
            // Generar código automático usando CodigoGenerator
            String codigoVenta = CodigoGenerator.generarCodigo(TABLA_VENTAS, "Código Venta");
            
            if (codigoProduccion == null || codigoProduccion.isEmpty()) {
                codigoProduccion = obtenerCodigoProduccion(venta.getProducto());
            }
            
            double costoDirecto = obtenerCostoDirectoPorProducto(venta.getProducto());
            double beneficio = calcularBeneficio(venta, costoDirecto);
            
            Map<String, String> filaVenta = construirFilaVentaConCodigo(venta, codigoVenta, codigoProduccion, costoDirecto, beneficio);
            
            boolean exito = CrearUtils.crearFila(TABLA_VENTAS, filaVenta);
            if (exito) {
                System.out.println("✅ Venta creada con código: " + codigoVenta);
            }
            return exito;
        } catch (Exception e) {
            System.err.println("Error al crear venta con código: " + e.getMessage());
            return false;
        }
    }
    
    public double obtenerCostoDirectoPorProducto(String producto) {
        return obtenerCostoDirecto(producto);
    }
    
    public boolean actualizarVenta(String codigoVenta, VentaModel venta) {
        try {
            Map<String, String> condiciones = Map.of("Código Venta", codigoVenta);
            Map<String, String> nuevosValores = construirActualizacion(venta);
            return ModificarUtils.modificarFila(TABLA_VENTAS, condiciones, nuevosValores);
        } catch (Exception e) {
            System.err.println("Error al actualizar venta: " + e.getMessage());
            return false;
        }
    }
    
    public boolean eliminarVenta(String codigoVenta) {
        try {
            Map<String, String> condiciones = Map.of("Código Venta", codigoVenta);
            return EliminarUtils.eliminarFila(TABLA_VENTAS, condiciones);
        } catch (Exception e) {
            System.err.println("Error al eliminar venta: " + e.getMessage());
            return false;
        }
    }
    
    public EstadisticasVenta calcularEstadisticas() {
        List<VentaModel> ventas = obtenerTodasLasVentas();
        return new EstadisticasCalculator().calcular(ventas);
    }
    
    public String buscarEnProduccion(String codigoProduccion, String campo) {
        return VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Código Producción", codigoProduccion, campo);
    }
    
    private VentaModel mapearAVentaModel(Map<String, String> fila) {
        VentaModel venta = new VentaModel();
        venta.setCodigoVenta(fila.getOrDefault("Código Venta", ""));
        venta.setCodigoProduccion(fila.getOrDefault("Código Producción", ""));
        venta.setFecha(fila.getOrDefault("Fecha", ""));
        venta.setCantidadVendida(ParseUtils.toDouble(fila.getOrDefault("Cantidad Vendida", "0")));
        venta.setPrecioVenta(ParseUtils.toDouble(fila.getOrDefault("Precio de Venta por Unidad", "0")));
        venta.setCostoDirecto(ParseUtils.toDouble(fila.getOrDefault("Costo Directo/U", "0")));
        venta.setBeneficio(ParseUtils.toDouble(fila.getOrDefault("Beneficio", "0")));
        venta.setProducto(obtenerNombreProducto(venta.getCodigoProduccion()));
        return venta;
    }
    
    private String obtenerCodigoProduccion(String producto) {
        String codigo = VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Producto", producto, "Código Producción");
        if (codigo != null && !codigo.isEmpty()) {
            return codigo;
        }
        
        return VerUtils.buscarPorCodigo("Recetas", "Producto", producto, "Código receta");
    }
    
    private double obtenerCostoDirecto(String producto) {
        String costo = VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Producto", producto, "Costo Directo/U");
        if (costo != null && !costo.isEmpty()) {
            return ParseUtils.toDouble(costo);
        }
        
        costo = VerUtils.buscarPorCodigo("Recetas", "Producto", producto, "Costo/U");
        return ParseUtils.toDouble(costo);
    }
    
    private String obtenerNombreProducto(String codigoProduccion) {
        String producto = VerUtils.buscarPorCodigo(TABLA_PRODUCCION, "Código Producción", codigoProduccion, "Producto");
        if (producto != null && !producto.isEmpty()) {
            return producto;
        }
        
        return VerUtils.buscarPorCodigo("Recetas", "Código receta", codigoProduccion, "Producto");
    }
    
    private double calcularBeneficio(VentaModel venta, double costoDirecto) {
        double ingresoTotal = venta.getPrecioVenta() * venta.getCantidadVendida();
        double costoTotal = costoDirecto * venta.getCantidadVendida();
        return ingresoTotal - costoTotal;
    }
    
    private Map<String, String> construirFilaVenta(VentaModel venta, String codigoProduccion, double costoDirecto, double beneficio) {
        Map<String, String> fila = new LinkedHashMap<>();
        fila.put("Código Venta", "Auto");
        fila.put("Código Producción", codigoProduccion);
        fila.put("Fecha", venta.getFecha());
        fila.put("Cantidad Vendida", String.format("%.0f", venta.getCantidadVendida()));
        fila.put("Precio de Venta por Unidad", String.format("%.4f", venta.getPrecioVenta()));
        fila.put("Costo Directo/U", String.format("%.4f", costoDirecto));
        fila.put("Beneficio", String.format("%.2f", beneficio));
        return fila;
    }
    
    private Map<String, String> construirFilaVentaConCodigo(VentaModel venta, String codigoVenta, String codigoProduccion, double costoDirecto, double beneficio) {
        Map<String, String> fila = new LinkedHashMap<>();
        fila.put("Código Venta", codigoVenta);
        fila.put("Código Producción", codigoProduccion);
        fila.put("Fecha", venta.getFecha());
        fila.put("Cantidad Vendida", String.format("%.0f", venta.getCantidadVendida()));
        fila.put("Precio de Venta por Unidad", String.format("%.4f", venta.getPrecioVenta()));
        fila.put("Costo Directo/U", String.format("%.4f", costoDirecto));
        fila.put("Beneficio", String.format("%.2f", beneficio));
        return fila;
    }
    
    private Map<String, String> construirActualizacion(VentaModel venta) {
        Map<String, String> cambios = new HashMap<>();
        cambios.put("Cantidad Vendida", String.format("%.0f", venta.getCantidadVendida()));
        cambios.put("Precio de Venta por Unidad", String.format("%.4f", venta.getPrecioVenta()));
        return cambios;
    }
}