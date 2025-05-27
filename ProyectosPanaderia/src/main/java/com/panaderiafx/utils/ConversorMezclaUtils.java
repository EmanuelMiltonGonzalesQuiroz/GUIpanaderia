package com.panaderiafx.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConversorMezclaUtils {

    public static int calcularProduccionDesdeMezclas(double cantidadMezclas, String codigoReceta) {
        double rendimiento = obtenerRendimientoReceta(codigoReceta);
        if (rendimiento <= 0) return 0;

        double librasPorMezcla = buscarEquivalenciaMezcla();
        if (librasPorMezcla <= 0) return 0;

        double harinaBaseLbs = obtenerHarinaBaseEnLibras(codigoReceta);
        if (harinaBaseLbs <= 0) return 0;

        double vecesReceta = (cantidadMezclas * librasPorMezcla) / harinaBaseLbs;
        return (int) Math.floor(vecesReceta * rendimiento);
    }

    public static double calcularMezclasDesdeProduccion(int cantidadProducida, String codigoReceta) {
        double rendimiento = obtenerRendimientoReceta(codigoReceta);
        if (rendimiento <= 0) return 0;

        double librasPorMezcla = buscarEquivalenciaMezcla();
        if (librasPorMezcla <= 0) return 0;

        double harinaBaseLbs = obtenerHarinaBaseEnLibras(codigoReceta);
        if (harinaBaseLbs <= 0) return 0;

        double vecesReceta = cantidadProducida / rendimiento;
        return (vecesReceta * harinaBaseLbs) / librasPorMezcla;
    }

    public static double obtenerHarinaBaseEnLibras(String codigoReceta) {
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> primeraFila = null;

        for (Map<String, String> fila : recetasIngredientes) {
            if (!codigoReceta.equalsIgnoreCase(fila.getOrDefault("Código receta", ""))) continue;

            if (primeraFila == null) primeraFila = fila;

            String codIngrediente = fila.get("Ingrediente");
            String unidad = fila.get("Unidades");
            double cantidad = parseDouble(fila.get("Cantidad"));
            String nombre = buscarNombreIngrediente(codIngrediente, ingredientes);

            if (nombre.toLowerCase(Locale.ROOT).contains("harina")) {
                return convertirAPesoLibras(cantidad, unidad); // usar solo la primera harina encontrada
            }
        }

        // Si no se encontró harina, usar el primer ingrediente de la receta
        if (primeraFila != null) {
            String codIngrediente = primeraFila.get("Ingrediente");
            String unidad = primeraFila.get("Unidades");
            double cantidad = parseDouble(primeraFila.get("Cantidad"));
            return convertirAPesoLibras(cantidad, unidad);
        }

        return 0;
    }

    public static double obtenerRendimientoReceta(String codigoReceta) {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        for (Map<String, String> fila : recetas) {
            if (fila.get("Código receta").equalsIgnoreCase(codigoReceta)) {
                return parseDouble(fila.get("Rendimiento"));
            }
        }
        return 0;
    }

    public static double buscarEquivalenciaMezcla() {
        List<Map<String, String>> conversiones = VerUtils.verTabla("TabladeConversión");
        for (Map<String, String> fila : conversiones) {
            if ("Mezcla".equalsIgnoreCase(fila.get("Unidad base")) &&
                "Libras".equalsIgnoreCase(fila.get("Unidad 2"))) {
                return parseDouble(fila.get("Equivalencia aproximada"));
            }
        }
        return 0;
    }

    public static double convertirAPesoLibras(double cantidad, String unidadOriginal) {
        unidadOriginal = unidadOriginal.trim().toLowerCase(Locale.ROOT);

        if (unidadOriginal.contains("libra")) return cantidad;
        if (unidadOriginal.contains("kilo")) return cantidad * 2.20462;
        if (unidadOriginal.contains("gramo")) return cantidad * 0.00220462;
        if (unidadOriginal.contains("onza")) return cantidad * 0.0625;

        return 0;
    }

    public static String buscarNombreIngrediente(String codigo, List<Map<String, String>> ingredientes) {
        for (Map<String, String> fila : ingredientes) {
            if (fila.get("Código").equalsIgnoreCase(codigo)) {
                return fila.get("Nombre");
            }
        }
        return "";
    }

    public static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) { 
            return 0;
        }
    }
}
