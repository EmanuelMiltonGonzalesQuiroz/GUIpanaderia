package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CostoIngredientePorRecetaUtils {

    private static final Map<String, Map<String, Double>> cachePorReceta = new HashMap<>();

    private static List<Map<String, String>> TABLA_RECETAS;
    private static List<Map<String, String>> TABLA_RECETAS_INGREDIENTES;
    private static List<Map<String, String>> TABLA_INGREDIENTES;

    private static void cargarTablas() {
        if (TABLA_RECETAS == null) {
            TABLA_RECETAS = VerUtils.verTabla("Recetas");
        }
        if (TABLA_RECETAS_INGREDIENTES == null) {
            TABLA_RECETAS_INGREDIENTES = VerUtils.verTabla("RecetasIngredientes");
        }
        if (TABLA_INGREDIENTES == null) {
            TABLA_INGREDIENTES = VerUtils.verTabla("Ingredientes");
        }
    }

    public static double calcular(String codReceta, String codIngrediente, double ignorado) {
        if (codReceta == null || codIngrediente == null) return 0;

        if (cachePorReceta.containsKey(codReceta) &&
                cachePorReceta.get(codReceta).containsKey(codIngrediente)) {
            return cachePorReceta.get(codReceta).get(codIngrediente);
        }

        cargarTablas();

        Map<String, String> receta = TABLA_RECETAS.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;

        List<Map<String, String>> ingredientesDeReceta = TABLA_RECETAS_INGREDIENTES.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .collect(Collectors.toList());

        Map<String, String> filaIngrediente = TABLA_INGREDIENTES.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) return 0;

        Map<String, String> detalleIngrediente = ingredientesDeReceta.stream()
                .filter(i -> codIngrediente.equals(i.get("Ingrediente")))
                .findFirst().orElse(null);
        if (detalleIngrediente == null) return 0;

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));
        double cantidadUsada = ParseUtils.toDouble(detalleIngrediente.getOrDefault("Cantidad", "0"));
        String unidadUsada = detalleIngrediente.getOrDefault("Unidades", "").trim();

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;

        double costo = cantidadConvertida * precio;

        cachePorReceta.computeIfAbsent(codReceta, k -> new HashMap<>()).put(codIngrediente, costo);

        return costo;
    }

    public static double calcularUnitarioDesdeReceta(String codReceta) {
        if (codReceta == null || codReceta.isBlank()) return 0;

        cargarTablas();

        Map<String, String> receta = TABLA_RECETAS.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;

        List<Map<String, String>> ingredientesDeReceta = TABLA_RECETAS_INGREDIENTES.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .collect(Collectors.toList());

        double suma = 0;
        for (Map<String, String> i : ingredientesDeReceta) {
            String codIng = i.getOrDefault("Ingrediente", "").trim();
            double costo = calcular(codReceta, codIng, 1);
            suma += costo;
        }

        return suma / rendimiento;
    }

    public static double calcularDesdeDatosDirectos(String codIngrediente, String unidadUsada, double cantidadUsada) {
        if (codIngrediente == null || unidadUsada == null || cantidadUsada <= 0) return 0;

        if (TABLA_INGREDIENTES == null) {
            TABLA_INGREDIENTES = VerUtils.verTabla("Ingredientes");
        }

        Map<String, String> filaIngrediente = TABLA_INGREDIENTES.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) return 0;

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;

        return cantidadConvertida * precio;
    }

    public static void limpiarCache() {
        cachePorReceta.clear();
        TABLA_RECETAS = null;
        TABLA_RECETAS_INGREDIENTES = null;
        TABLA_INGREDIENTES = null;
    }
}
