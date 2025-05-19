package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.List;
import java.util.Map;

public class CostosDirectosPorRecetaUtils {

    public static double calcular(String codigoProduccion, String codReceta, double cantidadProduccion) {
        if (codigoProduccion == null || codigoProduccion.isBlank()) return 0.0;
        if (codReceta == null || codReceta.isBlank()) return 0.0;
        if (cantidadProduccion <= 0) return 0.0;

        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");

        double rendimiento = recetas.stream()
                .filter(r -> codReceta.equals(r.getOrDefault("Código receta", "")))
                .map(r -> ParseUtils.toDouble(r.getOrDefault("Rendimiento", "0")))
                .findFirst().orElse(0.0);

        if (rendimiento <= 0) {
            System.out.printf("⚠️ Rendimiento inválido para receta %s (prod %s): %.2f%n", codReceta, codigoProduccion, rendimiento);
            return 0.0;
        }

        List<Map<String, String>> ingredientes = recetasIngredientes.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .toList();

        double sumaCostos = 0;
        for (Map<String, String> ing : ingredientes) {
            String codIng = ing.getOrDefault("Ingrediente", "").trim();
            if (!codIng.isEmpty()) {
                double costo = CostoIngredientePorRecetaUtils.calcular(codReceta, codIng, rendimiento);
                if (costo == 0) {
                    System.out.printf("⚠️ Costo 0 para ingrediente %s en receta %s (prod %s)%n", codIng, codReceta, codigoProduccion);
                }
                sumaCostos += costo;
            }
        }

        double costoUnitario = sumaCostos / rendimiento;
        double costoTotal = costoUnitario * cantidadProduccion;

        System.out.printf("✅ Costo total receta %s (%s): %.2f (unitario: %.4f * %.0f)%n",
                codReceta, codigoProduccion, costoTotal, costoUnitario, cantidadProduccion);

        CacheCostosDirectosUtils.guardar(codigoProduccion, costoTotal);
        CacheCostosDirectosUtils.guardarUnidad(codigoProduccion, costoUnitario);

        return costoTotal;
    }

    public static double calcularPorUnidad(String codigoProduccion, String codReceta) {
        if (codigoProduccion == null || codReceta == null || codReceta.isBlank()) return 0.0;

        if (CacheCostosDirectosUtils.contieneUnidad(codigoProduccion)) {
            return CacheCostosDirectosUtils.obtenerUnidad(codigoProduccion);
        }

        return calcular(codigoProduccion, codReceta, 1.0);
    }
}
