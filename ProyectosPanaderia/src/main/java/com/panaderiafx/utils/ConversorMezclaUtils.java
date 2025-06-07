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
                // NUEVO: Usar ConversorUtils mejorado para conversión precisa
                return convertirAPesoLibras(cantidad, unidad, nombre); // usar solo la primera harina encontrada
            }
        }

        // Si no se encontró harina, usar el primer ingrediente de la receta
        if (primeraFila != null) {
            String codIngrediente = primeraFila.get("Ingrediente");
            String unidad = primeraFila.get("Unidades");
            double cantidad = parseDouble(primeraFila.get("Cantidad"));
            String nombre = buscarNombreIngrediente(codIngrediente, ingredientes);
            return convertirAPesoLibras(cantidad, unidad, nombre);
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

    /**
     * VERSIÓN MEJORADA: Convierte cualquier unidad de peso a Libras usando ConversorUtils
     * @param cantidad Cantidad a convertir
     * @param unidadOriginal Unidad original (ej: "Gramos", "Kilos", "taza", "Saco")
     * @param ingrediente Nombre del ingrediente para conversiones específicas (opcional)
     * @return Cantidad convertida a Libras
     */
    public static double convertirAPesoLibras(double cantidad, String unidadOriginal, String ingrediente) {
        if (cantidad <= 0) return 0;
        
        String unidadNormalizada = normalizarUnidad(unidadOriginal);
        
        System.out.printf("🔄 [ConversorMezclaUtils] Convirtiendo: %.4f %s → Libras (ingrediente: %s)%n", 
                cantidad, unidadNormalizada, ingrediente != null ? ingrediente : "genérico");

        // Si ya está en libras, retornar directamente
        if (esUnidadLibras(unidadNormalizada)) {
            System.out.printf("✅ Ya en libras: %.4f%n", cantidad);
            return cantidad;
        }

        // Intentar conversión usando ConversorUtils mejorado
        Double resultado = ConversorUtils.convertir("Peso", unidadNormalizada, "Libras", cantidad, ingrediente);
        
        if (resultado != null && resultado > 0) {
            System.out.printf("✅ Conversión exitosa con ConversorUtils: %.4f Libras%n", resultado);
            return resultado;
        }

        // Fallback: Usar conversión manual (legacy) para compatibilidad
        System.out.printf("⚠️ ConversorUtils falló, usando conversión manual legacy%n");
        double resultadoLegacy = convertirAPesoLibrasLegacy(cantidad, unidadOriginal);
        
        if (resultadoLegacy > 0) {
            System.out.printf("✅ Conversión legacy exitosa: %.4f Libras%n", resultadoLegacy);
            return resultadoLegacy;
        }

        System.out.printf("❌ No se pudo convertir %s a Libras%n", unidadOriginal);
        return 0;
    }

    /**
     * Sobrecarga para mantener compatibilidad con código existente
     */
    public static double convertirAPesoLibras(double cantidad, String unidadOriginal) {
        return convertirAPesoLibras(cantidad, unidadOriginal, null);
    }

    /**
     * Conversión manual legacy (mantener como fallback)
     */
    private static double convertirAPesoLibrasLegacy(double cantidad, String unidadOriginal) {
        String unidad = unidadOriginal.trim().toLowerCase(Locale.ROOT);

        if (unidad.contains("libra")) return cantidad;
        if (unidad.contains("kilo")) return cantidad * 2.20462;
        if (unidad.contains("gramo")) return cantidad * 0.00220462;
        if (unidad.contains("onza") && !unidad.contains("líquida")) return cantidad * 0.0625;

        System.out.printf("⚠️ Unidad desconocida en conversión legacy: %s%n", unidadOriginal);
        return 0;
    }

    /**
     * Normaliza nombres de unidades para mejorar compatibilidad
     */
    private static String normalizarUnidad(String unidad) {
        if (unidad == null) return "";
        
        String normalizada = unidad.trim();
        
        // Normalizar variantes comunes
        return switch (normalizada.toLowerCase()) {
            case "g" -> "Gramos";
            case "kg" -> "Kilos";
            case "lb", "lbs" -> "Libras";
            case "oz" -> "Onzas";
            default -> normalizada;
        };
    }

    /**
     * Detecta si una unidad ya representa libras
     */
    private static boolean esUnidadLibras(String unidad) {
        String unidadLower = unidad.toLowerCase();
        return unidadLower.contains("libra") || 
               unidadLower.equals("lb") || 
               unidadLower.equals("lbs");
    }

    /**
     * NUEVA FUNCIÓN: Convierte cualquier unidad a otra usando ConversorUtils
     * Útil para futuras expansiones del sistema de mezclas
     */
    public static double convertirUnidades(double cantidad, String unidadOrigen, String unidadDestino, String ingrediente) {
        System.out.printf("🔄 [ConversorMezclaUtils] Conversión general: %.4f %s → %s (ingrediente: %s)%n", 
                cantidad, unidadOrigen, unidadDestino, ingrediente != null ? ingrediente : "genérico");

        // Determinar tipo lógico basado en unidades
        String tipoLogico = determinarTipoLogico(unidadOrigen, unidadDestino);
        
        Double resultado = ConversorUtils.convertir(tipoLogico, unidadOrigen, unidadDestino, cantidad, ingrediente);
        
        if (resultado != null) {
            System.out.printf("✅ Conversión general exitosa: %.4f %s%n", resultado, unidadDestino);
            return resultado;
        }

        System.out.printf("❌ Conversión general falló: %s → %s%n", unidadOrigen, unidadDestino);
        return 0;
    }

    /**
     * Determina el tipo lógico más apropiado para la conversión
     */
    private static String determinarTipoLogico(String unidadOrigen, String unidadDestino) {
        // Palabras clave para peso
        String[] palabrasPeso = {"gramo", "kilo", "libra", "onza", "g", "kg", "lb", "lbs", "oz"};
        // Palabras clave para volumen
        String[] palabrasVolumen = {"litro", "mililitro", "ml", "l", "galón", "onza líquida"};
        // Palabras clave para herramientas
        String[] palabrasHerramienta = {"taza", "saco", "caja", "paquete", "bote", "rollo", "bolsa", "galones"};

        boolean origenEsPeso = contieneAlgunaPalabra(unidadOrigen.toLowerCase(), palabrasPeso);
        boolean destinoEsPeso = contieneAlgunaPalabra(unidadDestino.toLowerCase(), palabrasPeso);
        
        boolean origenEsVolumen = contieneAlgunaPalabra(unidadOrigen.toLowerCase(), palabrasVolumen);
        boolean destinoEsVolumen = contieneAlgunaPalabra(unidadDestino.toLowerCase(), palabrasVolumen);
        
        boolean origenEsHerramienta = contieneAlgunaPalabra(unidadOrigen.toLowerCase(), palabrasHerramienta);
        boolean destinoEsHerramienta = contieneAlgunaPalabra(unidadDestino.toLowerCase(), palabrasHerramienta);

        // Lógica de determinación
        if (origenEsHerramienta || destinoEsHerramienta) return "Herramienta";
        if (origenEsPeso && destinoEsPeso) return "Peso";
        if (origenEsVolumen && destinoEsVolumen) return "Volumen";
        if ((origenEsPeso && destinoEsVolumen) || (origenEsVolumen && destinoEsPeso)) return "Peso"; // Conversión peso-volumen

        return "Peso"; // Default
    }

    /**
     * Verifica si un texto contiene alguna de las palabras especificadas
     */
    private static boolean contieneAlgunaPalabra(String texto, String[] palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) return true;
        }
        return false;
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

    /**
     * NUEVA FUNCIÓN: Limpiar caches relacionados
     */
    public static void limpiarCache() {
        ConversorUtils.limpiarCache();
        System.out.println("🧹 [ConversorMezclaUtils] Cache de conversiones limpiado.");
    }

    /**
     * NUEVA FUNCIÓN: Mostrar estadísticas de conversiones
     */
    public static void mostrarEstadisticas() {
        System.out.println("📊 [ConversorMezclaUtils] Estadísticas:");
        ConversorUtils.mostrarEstadisticasCache();
        
        // Mostrar equivalencia de mezcla actual
        double equivalencia = buscarEquivalenciaMezcla();
        System.out.printf("📏 Equivalencia Mezcla: 1 Mezcla = %.2f Libras%n", equivalencia);
    }
}