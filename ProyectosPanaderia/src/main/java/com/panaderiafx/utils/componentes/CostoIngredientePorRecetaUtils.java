package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import java.util.*;

public class CostoIngredientePorRecetaUtils {

    private static final Map<String, Map<String, Double>> cachePorReceta = new HashMap<>();
    
    // ❌ PROBLEMA: Variables estáticas que cachean datos sin invalidarse
    private static List<Map<String, String>> TABLA_RECETAS;
    private static List<Map<String, String>> TABLA_RECETAS_INGREDIENTES;
    private static List<Map<String, String>> TABLA_INGREDIENTES;

    private static void cargarTablas() {
        // ✅ SOLUCIÓN: Siempre recargar desde VerUtils para obtener datos frescos
        TABLA_RECETAS = VerUtils.verTabla("Recetas");
        TABLA_RECETAS_INGREDIENTES = VerUtils.verTabla("RecetasIngredientes");
        TABLA_INGREDIENTES = VerUtils.verTabla("Ingredientes");
    }

    public static double calcular(String codReceta, String codIngrediente, double ignorado) {
        if (codReceta == null || codIngrediente == null) return 0;

        cargarTablas(); // Ahora siempre recarga datos frescos

        Map<String, String> receta = TABLA_RECETAS.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) {
            System.out.printf("❌ Receta no encontrada: %s%n", codReceta);
            return 0;
        }

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) {
            System.out.printf("⚠️ Rendimiento inválido para receta %s: %.2f%n", codReceta, rendimiento);
            return 0;
        }

        Map<String, String> filaIngrediente = TABLA_INGREDIENTES.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) {
            System.out.printf("❌ Ingrediente no encontrado: %s%n", codIngrediente);
            return 0;
        }

        Map<String, String> detalleIngrediente = TABLA_RECETAS_INGREDIENTES.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")) && codIngrediente.equals(i.get("Ingrediente")))
                .findFirst().orElse(null);
        if (detalleIngrediente == null) {
            System.out.printf("⚠️ Ingrediente %s no asignado a receta %s%n", codIngrediente, codReceta);
            return 0;
        }

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));
        double cantidadUsada = ParseUtils.toDouble(detalleIngrediente.getOrDefault("Cantidad", "0"));
        String unidadUsada = detalleIngrediente.getOrDefault("Unidades", "").trim();

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) {
            System.out.printf("⚠️ Conversión fallida: %s -> %s (%s) en %s%n", unidadUsada, unidadIngrediente, codIngrediente, codReceta);
            return 0;
        }

        double costo = cantidadConvertida * precio;
        System.out.printf("🧮 Costo %s en %s: %.2f (%.2f * %.2f)%n", codIngrediente, codReceta, costo, cantidadConvertida, precio);

        // ✅ Opcional: Mantener cache pero con datos frescos
        cachePorReceta.computeIfAbsent(codReceta, k -> new HashMap<>()).put(codIngrediente, costo);
        return costo;
    }

    public static double calcularUnitarioDesdeReceta(String codReceta) {
        if (codReceta == null || codReceta.isBlank()) return 0;
        cargarTablas(); // Siempre datos frescos

        Map<String, String> receta = TABLA_RECETAS.stream()
                .filter(r -> codReceta.equals(r.get("Código receta")))
                .findFirst().orElse(null);
        if (receta == null) return 0;

        double rendimiento = ParseUtils.toDouble(receta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;

        List<Map<String, String>> ingredientes = TABLA_RECETAS_INGREDIENTES.stream()
                .filter(i -> codReceta.equals(i.get("Código receta")))
                .toList();

        double suma = 0;
        for (Map<String, String> i : ingredientes) {
            String codIng = i.getOrDefault("Ingrediente", "").trim();
            suma += calcular(codReceta, codIng, 1);
        }

        return suma / rendimiento;
    }

    public static double calcularDesdeDatosDirectos(String codIngrediente, String unidadUsada, double cantidadUsada) {
        if (codIngrediente == null || unidadUsada == null || cantidadUsada <= 0) return 0;

        // ✅ SOLUCIÓN: Siempre recargar datos frescos para este método crítico
        TABLA_INGREDIENTES = VerUtils.verTabla("Ingredientes");

        Map<String, String> filaIngrediente = TABLA_INGREDIENTES.stream()
                .filter(i -> codIngrediente.equals(i.get("Código")))
                .findFirst().orElse(null);
        if (filaIngrediente == null) {
            System.out.printf("❌ Ingrediente no encontrado en calcularDesdeDatosDirectos: %s%n", codIngrediente);
            return 0;
        }

        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));
        
        System.out.printf("💰 Precio obtenido para %s: %.2f%n", codIngrediente, precio);

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadUsada, unidadIngrediente, cantidadUsada, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida <= 0) {
            System.out.printf("⚠️ Conversión fallida en calcularDesdeDatosDirectos: %s -> %s%n", unidadUsada, unidadIngrediente);
            return 0;
        }

        double costoFinal = cantidadConvertida * precio;
        System.out.printf("🧮 Costo calculado para %s: %.2f (%.4f * %.2f)%n", codIngrediente, costoFinal, cantidadConvertida, precio);
        
        return costoFinal;
    }

    // ✅ MÉTODO MEJORADO: Limpiar todo el estado
    public static void limpiarCache() {
        System.out.println("🧹 Limpiando cache de costos...");
        cachePorReceta.clear();
        TABLA_RECETAS = null;
        TABLA_RECETAS_INGREDIENTES = null;
        TABLA_INGREDIENTES = null;
    }
    
    // ✅ NUEVO: Método para forzar actualización cuando cambien precios
    public static void actualizarDatos() {
        limpiarCache();
        cargarTablas();
        System.out.println("🔄 Datos de costos actualizados desde Excel");
    }
}