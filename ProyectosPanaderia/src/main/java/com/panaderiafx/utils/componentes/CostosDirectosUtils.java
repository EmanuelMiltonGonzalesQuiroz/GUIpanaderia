package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        if (fechaSeleccionada == null || tipo == null) return 0.0;

        // Limpiar caché antes de cada cálculo
        CacheCostosDirectosUtils.limpiar();

        // Cargar tablas una sola vez
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");

        // Crear mapa de rendimientos: Código receta → Rendimiento
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

        // Calcular suma de costos directos por cada receta producida en esa fecha
        return produccion.stream()
                .filter(f -> FechaUtils.coincide(f.getOrDefault("Fecha", ""), fechaSeleccionada, tipo))
                .mapToDouble(f -> {
                    String cod = f.getOrDefault("Código receta", "").trim();
                    if (cod.isEmpty()) return 0.0;

                    double rendimiento = mapaRendimientos.getOrDefault(cod, 0.0);
                    if (rendimiento <= 0) return 0.0;

                    return CostosDirectosPorRecetaUtils.calcular(cod, rendimiento);
                })
                .sum();
    }
}
