package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResumenGananciasUtils {

    public static void registrarGananciaProduccion(String fecha, Map<String, String> prod) {
        System.out.println("📝 Registrando ganancia para producción: " + prod);

        Map<String, String> fila = new LinkedHashMap<>();

        String codigoProduccion = prod.getOrDefault("Código Producción", "").trim();
        String codigoReceta = prod.getOrDefault("Código receta", "").trim();
        String version = prod.getOrDefault("Versión", "").trim();
        String producto = prod.getOrDefault("Producto", "").trim();
        double cantidad = ParseUtils.toDouble(prod.getOrDefault("Cantidad producida", "0"));

        // === PRECIO DE VENTA ===
        double precioVenta = ParseUtils.toDouble(prod.getOrDefault("Precio venta general", ""));
        if (precioVenta <= 0) {
            precioVenta = ParseUtils.toDouble(prod.getOrDefault("Precio de Venta por Unidad", ""));
        }

        if ((precioVenta <= 0 || version.isBlank() || producto.isBlank()) && !codigoReceta.isEmpty()) {
            List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
            Optional<Map<String, String>> receta = recetas.stream()
                    .filter(r -> r.getOrDefault("Código receta", "").equalsIgnoreCase(codigoReceta))
                    .findFirst();

            if (receta.isPresent()) {
                Map<String, String> r = receta.get();
                if (version.isBlank()) version = r.getOrDefault("Versión", "").trim();
                if (producto.isBlank()) producto = r.getOrDefault("Producto", "").trim();
                if (precioVenta <= 0) {
                    precioVenta = ParseUtils.toDouble(r.getOrDefault("Precio venta general", ""));
                }
            }
        }

        // === COSTOS ===
        double costoUnitario = ParseUtils.toDouble(prod.getOrDefault("Costo/U", "-1"));
        double costoTotal = 0.0;

        if (!codigoProduccion.isEmpty()) {
            if (costoUnitario < 0) {
                costoUnitario = CacheCostosDirectosUtils.obtenerUnidad(codigoProduccion);
            }
            costoTotal = CacheCostosDirectosUtils.obtener(codigoProduccion);
        }

        if (costoTotal <= 0 && cantidad > 0) {
            costoTotal = costoUnitario * cantidad;
        }

        // === GANANCIA ===
        double ganancia = (precioVenta * cantidad) - costoTotal;

        // === CREAR FILA ===
        fila.put("Código", CodigoGenerator.generarCodigo("Produccion", "Código"));
        fila.put("Fecha", fecha);
        fila.put("Código receta", codigoReceta);
        fila.put("Versión", version);
        fila.put("Producto", producto);
        fila.put("Cantidad producida", String.format("%.0f", cantidad));
        fila.put("Precio venta/U", String.format("%.2f", precioVenta));
        fila.put("Costo Directo/U", String.format("%.2f", costoUnitario));
        fila.put("Costo Total", String.format("%.2f", costoTotal));
        fila.put("Ganancia Total", String.format("%.2f", ganancia));

        CrearUtils.crearFila("Produccion", fila);
    }
}
