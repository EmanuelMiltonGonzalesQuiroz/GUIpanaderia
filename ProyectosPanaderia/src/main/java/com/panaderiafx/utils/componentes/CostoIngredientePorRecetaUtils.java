package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.List;
import java.util.Map;

public class CostoIngredientePorRecetaUtils {

    public static double calcular(String codReceta, String codIngrediente, double cantidadProduccion) {
        if (codReceta == null || codIngrediente == null || cantidadProduccion <= 0) return 0;

        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> receta = null;
        for (Map<String, String> r : recetas) {
            if (codReceta.equals(r.get("Código receta"))) {
                receta = r;
                break;
            }
        }
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;
        double factor = cantidadProduccion / rendimiento;

        Map<String, String> filaIngrediente = null;
        for (Map<String, String> i : ingredientes) {
            if (codIngrediente.equals(i.get("Código"))) {
                filaIngrediente = i;
                break;
            }
        }
        if (filaIngrediente == null) return 0;

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));

        Map<String, String> detalleIngrediente = null;
        for (Map<String, String> i : recetasIngredientes) {
            if (codReceta.equals(i.get("Código receta")) &&
                codIngrediente.equals(i.get("Ingrediente"))) {
                detalleIngrediente = i;
                break;
            }
        }
        if (detalleIngrediente == null) return 0;

        double cantidadUsada = ParseUtils.toDouble(detalleIngrediente.getOrDefault("Cantidad", "0"));
        String unidadUsada = detalleIngrediente.getOrDefault("Unidades", "").trim();

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;

        return cantidadConvertida * factor * precio;
    }
}
