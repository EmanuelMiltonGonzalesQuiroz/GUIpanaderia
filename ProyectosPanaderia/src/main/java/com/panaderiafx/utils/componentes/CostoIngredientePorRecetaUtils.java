package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.*;

public class CostoIngredientePorRecetaUtils {

    public static double calcular(String codReceta, String codIngrediente, double cantidadProduccion) {
        if (codReceta == null || codIngrediente == null || cantidadProduccion <= 0) return 0;

        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> receta = recetas.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;
        double factor = cantidadProduccion / rendimiento;

        Map<String, String> filaIngrediente = ingredientes.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) return 0;

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));

        Map<String, String> detalleIngrediente = recetasIngredientes.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")) &&
                             codIngrediente.equals(i.get("Ingrediente")))
                .findFirst().orElse(null);
        if (detalleIngrediente == null) return 0;

        double cantidadUsada = ParseUtils.toDouble(detalleIngrediente.getOrDefault("Cantidad", "0"));
        String unidadUsada = detalleIngrediente.getOrDefault("Unidades", "").trim();

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;

        return cantidadConvertida * factor * precio;
    }
}
