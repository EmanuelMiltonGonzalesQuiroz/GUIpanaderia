package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CostoIngredientePorRecetaUtils {

    private static final Map<String, Map<String, Double>> cachePorReceta = new HashMap<>();

    public static double calcular(String codReceta, String codIngrediente, double ignorado) {
        if (codReceta == null || codIngrediente == null) return 0;

        // Usar caché si ya se calculó
        String clave = codReceta;
        if (cachePorReceta.containsKey(clave) && cachePorReceta.get(clave).containsKey(codIngrediente)) {
            return cachePorReceta.get(clave).get(codIngrediente);
        }

        // Cargar tablas una sola vez
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> receta = recetas.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;

        // Filtrar ingredientes de esta receta
        List<Map<String, String>> ingredientesDeReceta = recetasIngredientes.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .collect(Collectors.toList());

        Map<String, String> filaIngrediente = ingredientes.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) return 0;

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));

        Map<String, String> detalleIngrediente = ingredientesDeReceta.stream()
                .filter(i -> codIngrediente.equals(i.get("Ingrediente")))
                .findFirst().orElse(null);
        if (detalleIngrediente == null) return 0;

        double cantidadUsada = ParseUtils.toDouble(detalleIngrediente.getOrDefault("Cantidad", "0"));
        String unidadUsada = detalleIngrediente.getOrDefault("Unidades", "").trim();

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;

        double costo = cantidadConvertida * precio;

        // Guardar en caché
        cachePorReceta.computeIfAbsent(clave, k -> new HashMap<>()).put(codIngrediente, costo);

        return costo;
    }

    public static void limpiarCache() {
        cachePorReceta.clear();
    }
    public static double calcularUnitarioDesdeReceta(String codReceta) {
        if (codReceta == null || codReceta.isBlank()) return 0;
    
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
    
        Map<String, String> receta = recetas.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;
    
        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;
    
        List<Map<String, String>> ingredientesDeReceta = recetasIngredientes.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .collect(Collectors.toList());
    
        double suma = 0;
        for (Map<String, String> i : ingredientesDeReceta) {
            String codIng = i.getOrDefault("Ingrediente", "").trim();
            double costo = calcular(codReceta, codIng, 1);  // usa el método ya definido
            suma += costo;
        }
    
        return suma / rendimiento;
    }
    public static double calcularDesdeDatosDirectos(String codIngrediente, String unidadUsada, double cantidadUsada) {
        if (codIngrediente == null || unidadUsada == null || cantidadUsada <= 0) return 0;
    
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");
    
        Map<String, String> filaIngrediente = ingredientes.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) return 0;
    
        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));
    
        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) return 0;
    
        return cantidadConvertida * precio;
    }
    
}
