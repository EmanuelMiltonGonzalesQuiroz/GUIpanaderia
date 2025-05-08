package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.*;

public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        CacheCostosDirectosUtils.limpiar();
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        Map<String, Double> mapaRendimientos = VerUtils.verTabla("Recetas").stream()
                .collect(HashMap::new,
                        (map, fila) -> {
                            String cod = fila.getOrDefault("Código receta", "").trim();
                            double rendimiento = ParseUtils.toDouble(fila.getOrDefault("Rendimiento", "0"));
                            map.put(cod, rendimiento);
                        },
                        HashMap::putAll);

        return produccion.stream()
                .filter(f -> FechaUtils.coincide(f.getOrDefault("Fecha", ""), fechaSeleccionada, tipo))
                .mapToDouble(f -> {
                    String cod = f.getOrDefault("Código receta", "");
                    double rendimiento = mapaRendimientos.getOrDefault(cod, 0.0);
                    return CostosDirectosPorRecetaUtils.calcular(cod, rendimiento);
                })
                .sum();
    }
}
