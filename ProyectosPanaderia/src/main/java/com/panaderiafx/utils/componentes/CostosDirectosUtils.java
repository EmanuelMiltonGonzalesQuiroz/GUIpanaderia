package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        if (fechaSeleccionada == null || tipo == null) return 0.0;

        System.out.println("🧹 Caché de costos directos limpiado.");
        CacheCostosDirectosUtils.limpiar();

        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");

        // Mapa para validar rendimiento de recetas
        Map<String, Double> mapaRendimientos = new HashMap<>();
        for (Map<String, String> fila : recetas) {
            String cod = fila.getOrDefault("Código receta", "").trim();
            if (!cod.isEmpty()) {
                double rendimiento = ParseUtils.toDouble(fila.getOrDefault("Rendimiento", "0"));
                if (rendimiento > 0) {
                    mapaRendimientos.put(cod, rendimiento);
                }
            }
        }

        System.out.printf("📄 Filas Producción: %d%n", produccion.size());

        double total = produccion.stream()
                .filter(f -> FechaUtils.coincide(f.getOrDefault("Fecha", ""), fechaSeleccionada, tipo))
                .mapToDouble(f -> {
                    String codProd = f.getOrDefault("Código Producción", "").trim();
                    String codReceta = f.getOrDefault("Código receta", "").trim();
                    double cantidad = ParseUtils.toDouble(f.getOrDefault("Cantidad producida", "0"));

                    if (codProd.isEmpty() || codReceta.isEmpty() || cantidad <= 0) {
                        System.out.printf("⚠️ Producción inválida: Código=%s Receta=%s Cantidad=%.2f%n", codProd, codReceta, cantidad);
                        return 0.0;
                    }

                    double costo = CostosDirectosPorRecetaUtils.calcular(codProd, codReceta, cantidad);

                    if (costo <= 0) {
                        System.out.printf("⚠️ Producción inválida: Código=%s Receta=%s Cantidad=%.2f CostoTotal=%.2f%n",
                                codProd, codReceta, cantidad, costo);
                    }

                    return costo;
                })
                .sum();

        System.out.printf("✅ Total costos directos: %.2f%n", total);
        CacheCostosDirectosUtils.set(total);
        return total;
    }
}
