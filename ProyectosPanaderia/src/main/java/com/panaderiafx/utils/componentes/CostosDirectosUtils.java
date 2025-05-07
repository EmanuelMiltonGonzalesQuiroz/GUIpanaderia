package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.*;

public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        CacheCostosDirectosUtils.limpiar();
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");

        return produccion.stream()
                .filter(f -> FechaUtils.coincide(f.getOrDefault("Fecha", ""), fechaSeleccionada, tipo))
                .mapToDouble(f -> {
                    String cod = f.getOrDefault("Código receta", "");
                    double cant = ParseUtils.toDouble(f.getOrDefault("Cantidad producida", "0"));
                    return CostosDirectosPorRecetaUtils.calcular(cod, cant);
                })
                .sum();
    }
}
