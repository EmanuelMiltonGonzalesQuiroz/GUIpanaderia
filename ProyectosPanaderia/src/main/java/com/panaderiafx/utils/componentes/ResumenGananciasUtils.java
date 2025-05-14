package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResumenGananciasUtils {

    public static void registrarGananciaProduccion(String fecha, Map<String, String> prod) {
        System.out.println("produccion: " + prod);

        Map<String, String> fila = new LinkedHashMap<>();

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

        // Si no se proporcionó el costo unitario, calcular desde receta
        if (costoUnitario < 0 && !codigoReceta.isEmpty()) {
            costoUnitario = CostosDirectosPorRecetaUtils.calcularPorUnidad(codigoReceta);
        }

        // Recalcular SIEMPRE el costo total si hay cantidad
        double costoTotal = (cantidad > 0 && costoUnitario >= 0)
                ? costoUnitario * cantidad
                : 0.0;

        // === GANANCIA ===
        double ganancia = (precioVenta * cantidad) - costoTotal;

        // === CREAR FILA ===
        fila.put("Código", CodigoGenerator.generarCodigo("GananciasProduccion", "Código"));
        fila.put("Fecha", fecha);
        fila.put("Código receta", codigoReceta);
        fila.put("Versión", version);
        fila.put("Producto", producto);
        fila.put("Cantidad producida", String.format("%.0f", cantidad));
        fila.put("Precio venta/U", String.format("%.2f", precioVenta));
        fila.put("Costo Directo/U", String.format("%.2f", costoUnitario));
        fila.put("Costo Total", String.format("%.2f", costoTotal));
        fila.put("Ganancia Total", String.format("%.2f", ganancia));

        CrearUtils.crearFila("GananciasProduccion", fila);
    }
}
