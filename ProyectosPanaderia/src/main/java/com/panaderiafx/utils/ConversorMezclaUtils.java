package com.panaderiafx.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utilidad para convertir mezclas en cantidad producida estimada,
 * leyendo datos desde Excel mediante VerUtils.
 */
public class ConversorMezclaUtils {

    /**
     * Calcula la cantidad producida estimada a partir del número de mezclas.
     *
     * @param cantidadMezclas número de mezclas utilizadas
     * @param codigoReceta    código de la receta (ej: "REC0010")
     * @return cantidad estimada de unidades producidas
     */
    public static int calcularProduccionDesdeMezclas(double cantidadMezclas, String codigoReceta) {
        double rendimiento = obtenerRendimientoReceta(codigoReceta);
        if (rendimiento <= 0) return 0;

        double librasPorMezcla = buscarEquivalenciaMezcla();
        if (librasPorMezcla <= 0) return 0;

        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> ingredienteFallback = null;

        for (Map<String, String> fila : recetasIngredientes) {
            if (!fila.getOrDefault("Código receta", "").equalsIgnoreCase(codigoReceta)) continue;

            if (ingredienteFallback == null) {
                ingredienteFallback = fila; // guardar primera fila
            }

            String codIngrediente = fila.get("Ingrediente");
            String unidad = fila.get("Unidades");
            double cantidadIngrediente = parseDouble(fila.get("Cantidad"));
            String nombreIngrediente = buscarNombreIngrediente(codIngrediente, ingredientes);

            if (nombreIngrediente.toLowerCase(Locale.ROOT).contains("harina")) {
                double librasIngrediente = convertirAPesoLibras(cantidadIngrediente, unidad);
                if (librasIngrediente <= 0) continue;

                double vecesReceta = (cantidadMezclas * librasPorMezcla) / librasIngrediente;
                return (int) Math.round(vecesReceta * rendimiento);
            }
        }

        // Fallback: usar el primer ingrediente si no hay harina
        if (ingredienteFallback != null) {
            String codIngrediente = ingredienteFallback.get("Ingrediente");
            String unidad = ingredienteFallback.get("Unidades");
            double cantidadIngrediente = parseDouble(ingredienteFallback.get("Cantidad"));

            double librasIngrediente = convertirAPesoLibras(cantidadIngrediente, unidad);
            if (librasIngrediente > 0) {
                System.err.println("⚠️ No se encontró ingrediente 'harina' en receta " + codigoReceta + ". Usando ingrediente alternativo: " + codIngrediente);
                double vecesReceta = (cantidadMezclas * librasPorMezcla) / librasIngrediente;
                return (int) Math.round(vecesReceta * rendimiento);
            }
        }

        return 0;
    }

    private static double obtenerRendimientoReceta(String codigoReceta) {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        for (Map<String, String> fila : recetas) {
            if (fila.get("Código receta").equalsIgnoreCase(codigoReceta)) {
                return parseDouble(fila.get("Rendimiento"));
            }
        }
        return 0;
    }

    private static double buscarEquivalenciaMezcla() {
        List<Map<String, String>> conversiones = VerUtils.verTabla("TabladeConversión");
        for (Map<String, String> fila : conversiones) {
            if ("Mezcla".equalsIgnoreCase(fila.get("Unidad base")) &&
                "Libras".equalsIgnoreCase(fila.get("Unidad 2"))) {
                return parseDouble(fila.get("Equivalencia aproximada"));
            }
        }
        return 0;
    }

    private static double convertirAPesoLibras(double cantidad, String unidadOriginal) {
        unidadOriginal = unidadOriginal.trim().toLowerCase(Locale.ROOT);

        if (unidadOriginal.contains("libra")) return cantidad;
        if (unidadOriginal.contains("kilo")) return cantidad * 2.20462;
        if (unidadOriginal.contains("gramo")) return cantidad * 0.00220462;

        return 0;
    }

    private static String buscarNombreIngrediente(String codigo, List<Map<String, String>> ingredientes) {
        for (Map<String, String> fila : ingredientes) {
            if (fila.get("Código").equalsIgnoreCase(codigo)) {
                return fila.get("Nombre");
            }
        }
        return "";
    }

    private static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
