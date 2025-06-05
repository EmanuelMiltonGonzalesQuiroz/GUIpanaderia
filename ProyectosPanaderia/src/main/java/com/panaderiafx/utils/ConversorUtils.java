package com.panaderiafx.utils;

import java.util.*;

public class ConversorUtils {

    private static final String TABLA = "TabladeConversión";
    private static final int MAX_CONVERSION_ATTEMPTS = 3; // Evitar loops infinitos

    private static final Map<String, Double> UNIDADES_ESTANDAR = Map.ofEntries(
            Map.entry("Kilos", 1000.0),
            Map.entry("Kilo", 1000.0),
            Map.entry("Gramos", 1.0),
            Map.entry("g", 1.0),
            Map.entry("Libras", 453.592),
            Map.entry("Libra", 453.592),
            Map.entry("Onzas", 28.3495),
            Map.entry("Onza", 28.3495),
            Map.entry("Litro", 1000.0),
            Map.entry("Litros", 1000.0),
            Map.entry("Mililitros", 1.0),
            Map.entry("ml", 1.0),
            Map.entry("Onza líquida", 29.5735)
    );

    private static List<Map<String, String>> CACHE_DATOS;
    private static final Map<String, Double> CACHE_EQUIVALENCIAS = new HashMap<>();

    public static Double convertir(String tipoLogico,
                                   String unidadOrigen,
                                   String unidadDestino,
                                   double cantidad,
                                   String ingrediente) {
        return convertir(tipoLogico, unidadOrigen, unidadDestino, cantidad, ingrediente, 0);
    }

    private static Double convertir(String tipoLogico,
                                   String unidadOrigen,
                                   String unidadDestino,
                                   double cantidad,
                                   String ingrediente,
                                   int attempts) {

        if (attempts >= MAX_CONVERSION_ATTEMPTS) {
            System.out.printf("🛑 Máximo de intentos alcanzado (%d) para evitar loop infinito%n", MAX_CONVERSION_ATTEMPTS);
            return null;
        }

        if (unidadOrigen.equalsIgnoreCase(unidadDestino)) {
            System.out.printf("⚠️ Unidades iguales [%s], retorno directo: %.2f%n", unidadOrigen, cantidad);
            return cantidad;
        }

        // 1. Intentar conversión rápida con mapas estáticos
        Double resultadoRapido = intentarConversionRapida(unidadOrigen, unidadDestino, cantidad);
        if (resultadoRapido != null) {
            return resultadoRapido;
        }

        // 2. Intentar conversión original de ConversorUtils (legacy)
        Double resultadoLegacy = intentarConversionLegacy(tipoLogico, unidadOrigen, unidadDestino, cantidad, ingrediente);
        if (resultadoLegacy != null) {
            return resultadoLegacy;
        }

        // 3. Usar conversor inteligente como fallback
        if (attempts == 0) {
            System.out.printf("🔄 Usando conversor inteligente: %s → %s%n", unidadOrigen, unidadDestino);
            Double resultadoInteligente = ConversorInteligenteUtils.convertir(unidadOrigen, unidadDestino, cantidad, ingrediente);
            if (resultadoInteligente != null) {
                return resultadoInteligente;
            }
        }

        // 4. Intentar conversión vía Gramos como unidad intermedia
        if (attempts <= 1) {
            Double resultadoViagramos = intentarConversionViaGramos(tipoLogico, unidadOrigen, unidadDestino, cantidad, ingrediente, attempts);
            if (resultadoViagramos != null) {
                return resultadoViagramos;
            }
        }

        System.out.printf("❌ Todas las estrategias de conversión fallaron para %s → %s%n", unidadOrigen, unidadDestino);
        return null;
    }

    private static Double intentarConversionRapida(String unidadOrigen, String unidadDestino, double cantidad) {
        // Conversión directa entre unidades estándar del mismo tipo
        if (UNIDADES_ESTANDAR.containsKey(unidadOrigen) && UNIDADES_ESTANDAR.containsKey(unidadDestino)) {
            // Determinar si son del mismo tipo (peso vs volumen)
            boolean origenEsPeso = esPeso(unidadOrigen);
            boolean destinoEsPeso = esPeso(unidadDestino);
            
            if (origenEsPeso == destinoEsPeso) {
                double base = cantidad * UNIDADES_ESTANDAR.get(unidadOrigen);
                double resultado = base / UNIDADES_ESTANDAR.get(unidadDestino);
                System.out.printf("⚡ Conversión rápida: %.4f %s = %.4f unidades base = %.4f %s%n", 
                        cantidad, unidadOrigen, base, resultado, unidadDestino);
                return resultado;
            }
        }
        return null;
    }

    private static boolean esPeso(String unidad) {
        return Set.of("Kilos", "Kilo", "Gramos", "g", "Libras", "Libra", "Onzas", "Onza").stream()
                .anyMatch(u -> u.equalsIgnoreCase(unidad));
    }

    private static Double intentarConversionLegacy(String tipoLogico, String unidadOrigen, String unidadDestino, 
                                                  double cantidad, String ingrediente) {
        System.out.printf("🔧 Intentando conversión legacy: %s → %s%n", unidadOrigen, unidadDestino);
        
        List<Map<String, String>> datos = obtenerDatos();

        List<String> tiposPermitidos = switch (tipoLogico) {
            case "Herramienta" -> List.of("Herramienta", "Peso", "Volumen");
            case "Peso" -> List.of("Peso", "Herramienta", "Conversión");
            case "Volumen" -> List.of("Volumen", "Herramienta", "Conversión");
            default -> List.of(tipoLogico, "Conversión", "Herramienta");
        };

        Double base = convertirAUnidadBase(cantidad, unidadOrigen, ingrediente, tiposPermitidos, datos);
        if (base == null) {
            System.out.printf("❌ No se pudo convertir %s a unidad base%n", unidadOrigen);
            return null;
        }

        Double divisor = equivalenciaParaUnidad(unidadDestino, ingrediente, tiposPermitidos, datos);
        if (divisor == null || divisor <= 0) {
            System.out.printf("❌ No se pudo obtener equivalencia para %s%n", unidadDestino);
            return null;
        }

        double resultado = base / divisor;
        System.out.printf("✅ Conversión legacy exitosa: %.4f (base) ÷ %.4f = %.4f %s%n", 
                base, divisor, resultado, unidadDestino);
        return resultado;
    }

    private static Double intentarConversionViaGramos(String tipoLogico, String unidadOrigen, String unidadDestino, 
                                                     double cantidad, String ingrediente, int attempts) {
        System.out.printf("🔄 Intentando conversión vía Gramos: %s → Gramos → %s%n", unidadOrigen, unidadDestino);
        
        // Paso 1: Convertir origen → Gramos
        Double gramos = null;
        if (!unidadOrigen.equalsIgnoreCase("Gramos")) {
            gramos = convertir(tipoLogico, unidadOrigen, "Gramos", cantidad, ingrediente, attempts + 1);
        } else {
            gramos = cantidad;
        }
        
        if (gramos == null) {
            System.out.printf("❌ No se pudo convertir %s → Gramos%n", unidadOrigen);
            return null;
        }

        // Paso 2: Convertir Gramos → destino
        if (!unidadDestino.equalsIgnoreCase("Gramos")) {
            Double resultado = convertir(tipoLogico, "Gramos", unidadDestino, gramos, ingrediente, attempts + 1);
            if (resultado != null) {
                System.out.printf("✅ Conversión vía Gramos exitosa: %.4f %s → %.4f Gramos → %.4f %s%n", 
                        cantidad, unidadOrigen, gramos, resultado, unidadDestino);
                return resultado;
            }
        }

        return gramos; // Si destino es Gramos, retornar el valor intermedio
    }

    private static Double convertirAUnidadBase(double cantidad,
                                               String unidad,
                                               String ingrediente,
                                               List<String> tiposPermitidos,
                                               List<Map<String, String>> datos) {
        Double factor = equivalenciaParaUnidad(unidad, ingrediente, tiposPermitidos, datos);
        if (factor == null || factor <= 0) {
            System.out.printf("❌ No se pudo obtener factor para unidad origen: %s%n", unidad);
            return null;
        }

        double resultado = cantidad * factor;
        System.out.printf("➡️ %.4f %s * %.4f = %.4f (unidad base)%n", cantidad, unidad, factor, resultado);
        return resultado;
    }

    private static Double equivalenciaParaUnidad(String unidad,
                                                 String ingrediente,
                                                 List<String> tiposPermitidos,
                                                 List<Map<String, String>> datos) {
        String clave = (ingrediente != null ? ingrediente : "GENERIC") + "|" + unidad;
        if (CACHE_EQUIVALENCIAS.containsKey(clave)) {
            Double cached = CACHE_EQUIVALENCIAS.get(clave);
            System.out.printf("💾 Cache hit para %s: %.4f%n", unidad, cached);
            return cached;
        }

        // Buscar primero en unidades estándar
        if (UNIDADES_ESTANDAR.containsKey(unidad)) {
            Double val = UNIDADES_ESTANDAR.get(unidad);
            CACHE_EQUIVALENCIAS.put(clave, val);
            System.out.printf("📏 Unidad estándar %s: %.4f%n", unidad, val);
            return val;
        }

        // Buscar en la tabla Excel
        Double resultado = datos.stream()
                .filter(d -> tiposPermitidos.contains(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
                .sorted(Comparator.comparing(d -> {
                    String ingr = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    // Priorizar coincidencias exactas de ingrediente
                    if (ingrediente != null && ingrediente.equalsIgnoreCase(ingr)) return 0;
                    if (ingr.isEmpty() || "—".equals(ingr)) return 1;
                    return 2;
                }))
                .map(d -> {
                    double val = extraerNumero(d.get("Equivalencia aproximada"));
                    String ingr = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    System.out.printf("🔍 Candidato: %s = %.4f (ingrediente: %s)%n", unidad, val, ingr.isEmpty() ? "genérico" : ingr);
                    return val;
                })
                .filter(val -> val > 0)
                .findFirst()
                .orElse(null);

        if (resultado != null) {
            CACHE_EQUIVALENCIAS.put(clave, resultado);
            System.out.printf("✅ Equivalencia encontrada para %s: %.4f%n", unidad, resultado);
        } else {
            System.out.printf("❌ No se encontró equivalencia para %s en tipos %s%n", unidad, tiposPermitidos);
        }

        return resultado;
    }

    private static double extraerNumero(String entrada) {
        try {
            if (entrada == null || entrada.trim().isEmpty()) return 0;
            String limpio = entrada.replace(",", ".").replaceAll("[^0-9.]", "").trim();
            if (limpio.isEmpty()) return 0;
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            System.err.printf("⚠️ Error al parsear número: '%s' → %s%n", entrada, e.getMessage());
            return 0;
        }
    }

    private static List<Map<String, String>> obtenerDatos() {
        if (CACHE_DATOS == null) {
            CACHE_DATOS = VerUtils.verTabla(TABLA);
            System.out.println("📥 [ConversorUtils] Datos de conversión obtenidos: " + CACHE_DATOS.size() + " filas.");
        }
        return CACHE_DATOS;
    }

    // Método público para limpiar caches
    public static void limpiarCache() {
        CACHE_DATOS = null;
        CACHE_EQUIVALENCIAS.clear();
        System.out.println("🧹 Cache del ConversorUtils limpiado.");
    }

    // Método público para obtener estadísticas de cache
    public static void mostrarEstadisticasCache() {
        System.out.printf("📊 Cache equivalencias: %d entradas%n", CACHE_EQUIVALENCIAS.size());
        System.out.printf("📊 Cache datos: %s%n", CACHE_DATOS != null ? CACHE_DATOS.size() + " filas" : "no cargado");
    }
}