package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.List;
import java.util.Map;

public class CostosDirectosPorRecetaUtils {

    public static double calcular(String codReceta, double ignorado) {
        if (codReceta == null || codReceta.isBlank()) return 0.0;

        // Cargar tablas necesarias una sola vez
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");

        double rendimiento = recetas.stream()
                .filter(r -> codReceta.equals(r.getOrDefault("Código receta", "")))
                .map(r -> ParseUtils.toDouble(r.getOrDefault("Rendimiento", "0")))
                .findFirst().orElse(0.0);

        if (rendimiento <= 0) return 0.0;

        double cantidadProduccion = produccion.stream()
                .filter(p -> codReceta.equals(p.getOrDefault("Código receta", "")))
                .map(p -> ParseUtils.toDouble(p.getOrDefault("Cantidad producida", "0")))
                .reduce((a, b) -> b) // tomar la última ocurrencia si hay varias
                .orElse(0.0);

        if (CacheCostosDirectosUtils.contiene(codReceta, cantidadProduccion)) {
            return CacheCostosDirectosUtils.obtener(codReceta, cantidadProduccion);
        }

        List<Map<String, String>> ingredientes = recetasIngredientes.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .toList();

        double sumaCostos = 0;
        for (Map<String, String> ing : ingredientes) {
            String codIng = ing.getOrDefault("Ingrediente", "").trim();
            if (!codIng.isEmpty()) {
                double costo = CostoIngredientePorRecetaUtils.calcular(codReceta, codIng, rendimiento);
                sumaCostos += costo;
            }
        }

        double costoUnitario = sumaCostos / rendimiento;
        double costoTotal = costoUnitario * cantidadProduccion;

        CacheCostosDirectosUtils.guardar(codReceta, cantidadProduccion, costoTotal);
        CacheCostosDirectosUtils.guardarUnidad(codReceta, costoUnitario);

        return costoTotal;
    }

    public static double calcularPorUnidad(String codReceta) {
        if (codReceta == null || codReceta.isBlank()) return 0.0;

        if (CacheCostosDirectosUtils.contieneUnidad(codReceta)) {
            return CacheCostosDirectosUtils.obtenerUnidad(codReceta);
        }

        return calcular(codReceta, 1.0); // segundo argumento ignorado
    }
}
