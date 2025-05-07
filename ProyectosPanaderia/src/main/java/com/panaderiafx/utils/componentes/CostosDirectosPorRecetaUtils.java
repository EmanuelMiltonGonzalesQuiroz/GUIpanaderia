package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.*;

public class CostosDirectosPorRecetaUtils {

    public static double calcular(String codReceta, double cantidadProducida) {
        if (CacheCostosDirectosUtils.contiene(codReceta, cantidadProducida)) {
            return CacheCostosDirectosUtils.obtener(codReceta, cantidadProducida);
        }

        List<Map<String, String>> ingredientes = VerUtils.verTabla("RecetasIngredientes").stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .toList();

        double total = 0;
        for (Map<String, String> ing : ingredientes) {
            String codIng = ing.getOrDefault("Ingrediente", "");
            double costo = CostoIngredientePorRecetaUtils.calcular(codReceta, codIng, cantidadProducida);
            total += costo;
        }

        // También guardamos el costo por unidad (si cantidad > 0)
        double costoPorUnidad = cantidadProducida > 0 ? total / cantidadProducida : 0.0;

        CacheCostosDirectosUtils.guardar(codReceta, cantidadProducida, total);
        CacheCostosDirectosUtils.guardarUnidad(codReceta, costoPorUnidad); // Nueva función auxiliar si no existe

        return total;
    }

    public static double calcularPorUnidad(String codReceta) {
        if (CacheCostosDirectosUtils.contieneUnidad(codReceta)) {
            return CacheCostosDirectosUtils.obtenerUnidad(codReceta);
        }

        // Si no hay unidad cacheada, intentar calcular desde producción mínima conocida (por ejemplo, 1 unidad)
        return calcular(codReceta, 1.0);
    }
}
