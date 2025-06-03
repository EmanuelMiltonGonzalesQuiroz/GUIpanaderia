package com.panaderiafx.utils;

import java.util.*;

public class ConversorUtils2 {

    private static final String TABLA = "TabladeConversión";

    // ✅ Unidades estándar base 
    private static final Map<String, Double> UNIDADES_PESO = Map.ofEntries(
            Map.entry("Kilos", 1000.0),
            Map.entry("Gramos", 1.0),
            Map.entry("g", 1.0),
            Map.entry("Libras", 453.592),
            Map.entry("Libra", 453.592),
            Map.entry("Onzas", 28.3495),
            Map.entry("Onza", 28.3495)
    );
    
    private static final Map<String, Double> UNIDADES_VOLUMEN = Map.ofEntries(
            Map.entry("Litro", 1000.0),
            Map.entry("Mililitros", 1.0),
            Map.entry("ml", 1.0),
            Map.entry("Onza líquida", 29.5735),
            Map.entry("Onza", 29.5735)  // Asumimos líquida si no se especifica
    );
    
    // Combinar todas las unidades estándar
    private static final Map<String, Double> UNIDADES_ESTANDAR = new HashMap<>();
    static {
        UNIDADES_ESTANDAR.putAll(UNIDADES_PESO);
        UNIDADES_ESTANDAR.putAll(UNIDADES_VOLUMEN);
    }

    // ✅ Factores de conversión agua (1g = 1ml para agua)
    private static final double FACTOR_AGUA_PESO_VOLUMEN = 1.0; // 1g agua = 1ml agua

    private static List<Map<String, String>> CACHE_DATOS;
    private static final Map<String, Double> CACHE_EQUIVALENCIAS = new HashMap<>();

    public static Double convertir(String tipoLogico,
                                   String unidadOrigen,
                                   String unidadDestino,
                                   double cantidad,
                                   String ingrediente) {

        if (unidadOrigen.equalsIgnoreCase(unidadDestino)) {
            System.out.printf("⚠️ Unidades iguales [%s], retorno directo: %.2f%n", unidadOrigen, cantidad);
            return cantidad;
        }

        System.out.printf("🔄 Convirtiendo: %.2f %s → %s (Ingrediente: %s, Tipo: %s)%n", 
                         cantidad, unidadOrigen, unidadDestino, ingrediente, tipoLogico);

        List<Map<String, String>> datos = obtenerDatos();

        // Permitir conversión entre todos los tipos
        List<String> tiposPermitidos = List.of("Peso", "Volumen", "Herramienta", "Conversión");

        // ✅ ESTRATEGIA DE CONVERSIÓN OPTIMIZADA:
        // 1. Convertir origen a gramos (base peso) usando ingrediente específico o fallback
        Double valorEnGramos = convertirAGramos(cantidad, unidadOrigen, ingrediente, datos);
        if (valorEnGramos == null) {
            System.out.printf("❌ No se pudo convertir %s a gramos%n", unidadOrigen);
            return null;
        }

        // 2. Convertir de gramos a unidad destino
        Double resultado = convertirDesdeGramos(valorEnGramos, unidadDestino, ingrediente, datos);
        if (resultado == null) {
            System.out.printf("❌ No se pudo convertir desde gramos a %s%n", unidadDestino);
            return null;
        }

        System.out.printf("✅ Conversión exitosa: %.2f %s = %.4f %s%n", 
                         cantidad, unidadOrigen, resultado, unidadDestino);
        
        return resultado;
    }

    /**
     * Convierte cualquier unidad a gramos (unidad base)
     */
    private static Double convertirAGramos(double cantidad, String unidad, String ingrediente, List<Map<String, String>> datos) {
        
        // 1. Si ya está en unidad de peso estándar
        if (esUnidadDePeso(unidad)) {
            Double factor = buscarEnUnidadesPeso(unidad);
            if (factor != null) {
                double resultado = cantidad * factor;
                System.out.printf("🔢 Peso estándar: %.2f %s * %.4f = %.4f gramos%n", cantidad, unidad, factor, resultado);
                return resultado;
            }
        }

        // 2. Si es unidad de volumen, convertir usando densidad específica o agua
        if (esUnidadDeVolumen(unidad)) {
            return convertirVolumenAGramos(cantidad, unidad, ingrediente, datos);
        }

        // 3. Si es herramienta, buscar en tabla
        return convertirHerramientaAGramos(cantidad, unidad, ingrediente, datos);
    }

    /**
     * Convierte desde gramos a cualquier unidad destino
     */
    private static Double convertirDesdeGramos(double gramos, String unidadDestino, String ingrediente, List<Map<String, String>> datos) {
        
        // 1. Si destino es peso estándar
        if (esUnidadDePeso(unidadDestino)) {
            Double factor = buscarEnUnidadesPeso(unidadDestino);
            if (factor != null) {
                double resultado = gramos / factor;
                System.out.printf("🔢 A peso estándar: %.4f gramos / %.4f = %.4f %s%n", gramos, factor, resultado, unidadDestino);
                return resultado;
            }
        }

        // 2. Si destino es volumen, usar densidad o agua
        if (esUnidadDeVolumen(unidadDestino)) {
            return convertirGramosAVolumen(gramos, unidadDestino, ingrediente, datos);
        }

        // 3. Si destino es herramienta (conversión inversa)
        return convertirGramosAHerramienta(gramos, unidadDestino, ingrediente, datos);
    }

    /**
     * Convierte volumen a gramos usando densidad específica o agua como fallback
     */
    private static Double convertirVolumenAGramos(double cantidad, String unidad, String ingrediente, List<Map<String, String>> datos) {
        
        // Primero convertir a mililitros
        Double factorVolumen = buscarEnUnidadesVolumen(unidad);
        if (factorVolumen == null) return null;
        
        double mililitros = cantidad * factorVolumen;
        System.out.printf("📏 Volumen: %.2f %s = %.4f ml%n", cantidad, unidad, mililitros);

        // Buscar densidad específica del ingrediente
        if (ingrediente != null && !ingrediente.trim().isEmpty()) {
            Optional<Double> densidadEspecifica = datos.stream()
                .filter(d -> "Conversión".equals(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> "ml".equalsIgnoreCase(d.get("Unidad base")) || "Mililitros".equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ("g".equalsIgnoreCase(d.get("Unidad 2")) || "Gramos".equalsIgnoreCase(d.get("Unidad 2"))))
                .filter(d -> ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> extraerNumero(d.get("Equivalencia aproximada")))
                .filter(val -> val > 0)
                .findFirst();

            if (densidadEspecifica.isPresent()) {
                double gramos = mililitros * densidadEspecifica.get();
                System.out.printf("🧪 Densidad específica %s: %.4f ml * %.4f = %.4f gramos%n", 
                                 ingrediente, mililitros, densidadEspecifica.get(), gramos);
                return gramos;
            }
        }

        // Fallback: usar densidad del agua (1ml = 1g)
        double gramos = mililitros * FACTOR_AGUA_PESO_VOLUMEN;
        System.out.printf("💧 Usando densidad agua: %.4f ml * %.2f = %.4f gramos%n", 
                         mililitros, FACTOR_AGUA_PESO_VOLUMEN, gramos);
        return gramos;
    }

    /**
     * Convierte gramos a volumen usando densidad específica o agua como fallback
     */
    private static Double convertirGramosAVolumen(double gramos, String unidadDestino, String ingrediente, List<Map<String, String>> datos) {
        
        // Buscar densidad específica del ingrediente
        Double densidad = FACTOR_AGUA_PESO_VOLUMEN; // Default: agua
        
        if (ingrediente != null && !ingrediente.trim().isEmpty()) {
            Optional<Double> densidadEspecifica = datos.stream()
                .filter(d -> "Conversión".equals(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> "ml".equalsIgnoreCase(d.get("Unidad base")) || "Mililitros".equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ("g".equalsIgnoreCase(d.get("Unidad 2")) || "Gramos".equalsIgnoreCase(d.get("Unidad 2"))))
                .filter(d -> ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> extraerNumero(d.get("Equivalencia aproximada")))
                .filter(val -> val > 0)
                .findFirst();

            if (densidadEspecifica.isPresent()) {
                densidad = densidadEspecifica.get();
                System.out.printf("🧪 Usando densidad específica de %s: %.4f%n", ingrediente, densidad);
            }
        }

        // Convertir a mililitros
        double mililitros = gramos / densidad;
        System.out.printf("📏 A volumen: %.4f gramos / %.4f = %.4f ml%n", gramos, densidad, mililitros);

        // Convertir a unidad destino
        Double factorDestino = buscarEnUnidadesVolumen(unidadDestino);
        if (factorDestino == null) return null;

        double resultado = mililitros / factorDestino;
        System.out.printf("🔢 A unidad final: %.4f ml / %.4f = %.4f %s%n", mililitros, factorDestino, resultado, unidadDestino);
        return resultado;
    }

    /**
     * Convierte herramienta a gramos
     */
    private static Double convertirHerramientaAGramos(double cantidad, String unidad, String ingrediente, List<Map<String, String>> datos) {
        
        // 1. Buscar conversión específica para el ingrediente
        if (ingrediente != null && !ingrediente.trim().isEmpty()) {
            Optional<Double> conversionEspecifica = datos.stream()
                .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> {
                    double equiv = extraerNumero(d.get("Equivalencia aproximada"));
                    String unidad2 = d.get("Unidad 2");
                    
                    System.out.printf("🔍 Encontrada conversión específica: 1 %s (%s) = %.2f %s%n", 
                                     unidad, ingrediente, equiv, unidad2);
                    
                    // Si equivalencia está en gramos
                    if ("g".equalsIgnoreCase(unidad2) || "Gramos".equalsIgnoreCase(unidad2)) {
                        return equiv;
                    }
                    
                    // Si está en otra unidad, convertir
                    Double factor = buscarEnUnidadesEstandar(unidad2);
                    return factor != null ? equiv * factor : equiv;
                })
                .filter(val -> val > 0)
                .findFirst();

            if (conversionEspecifica.isPresent()) {
                double gramos = cantidad * conversionEspecifica.get();
                System.out.printf("🥄 Herramienta específica (%s): %.2f %s * %.4f = %.4f gramos%n", 
                                 ingrediente, cantidad, unidad, conversionEspecifica.get(), gramos);
                return gramos;
            }
        }

        // 2. Buscar conversión genérica (sin ingrediente específico)
        Optional<Double> conversionGenerica = datos.stream()
            .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
            .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
            .filter(d -> {
                String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                return ing.isEmpty() || "—".equals(ing);
            })
            .map(d -> {
                double equiv = extraerNumero(d.get("Equivalencia aproximada"));
                String unidad2 = d.get("Unidad 2");
                
                System.out.printf("🔍 Encontrada conversión genérica: 1 %s = %.2f %s%n", 
                                 unidad, equiv, unidad2);
                
                if ("g".equalsIgnoreCase(unidad2) || "Gramos".equalsIgnoreCase(unidad2)) {
                    return equiv;
                }
                
                Double factor = buscarEnUnidadesEstandar(unidad2);
                return factor != null ? equiv * factor : equiv;
            })
            .filter(val -> val > 0)
            .findFirst();

        if (conversionGenerica.isPresent()) {
            double gramos = cantidad * conversionGenerica.get();
            System.out.printf("🥄 Herramienta genérica: %.2f %s * %.4f = %.4f gramos%n", 
                             cantidad, unidad, conversionGenerica.get(), gramos);
            return gramos;
        }

        System.out.printf("❌ No se encontró conversión para herramienta: %s%n", unidad);
        return null;
    }

    /**
     * Convierte gramos a herramienta (conversión inversa)
     */
    private static Double convertirGramosAHerramienta(double gramos, String unidadDestino, String ingrediente, List<Map<String, String>> datos) {
        
        // Similar a convertirHerramientaAGramos pero inversa
        if (ingrediente != null && !ingrediente.trim().isEmpty()) {
            Optional<Double> factorEspecifico = datos.stream()
                .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
                .filter(d -> unidadDestino.equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> {
                    double equiv = extraerNumero(d.get("Equivalencia aproximada"));
                    String unidad2 = d.get("Unidad 2");
                    
                    if ("g".equalsIgnoreCase(unidad2) || "Gramos".equalsIgnoreCase(unidad2)) {
                        return equiv;
                    }
                    
                    Double factor = buscarEnUnidadesEstandar(unidad2);
                    return factor != null ? equiv * factor : equiv;
                })
                .filter(val -> val > 0)
                .findFirst();

            if (factorEspecifico.isPresent()) {
                double resultado = gramos / factorEspecifico.get();
                System.out.printf("🔄 Gramos a herramienta (%s): %.4f / %.4f = %.4f %s%n", 
                                 ingrediente, gramos, factorEspecifico.get(), resultado, unidadDestino);
                return resultado;
            }
        }

        // Conversión genérica
        Optional<Double> factorGenerico = datos.stream()
            .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
            .filter(d -> unidadDestino.equalsIgnoreCase(d.get("Unidad base")))
            .filter(d -> {
                String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                return ing.isEmpty() || "—".equals(ing);
            })
            .map(d -> {
                double equiv = extraerNumero(d.get("Equivalencia aproximada"));
                String unidad2 = d.get("Unidad 2");
                
                if ("g".equalsIgnoreCase(unidad2) || "Gramos".equalsIgnoreCase(unidad2)) {
                    return equiv;
                }
                
                Double factor = buscarEnUnidadesEstandar(unidad2);
                return factor != null ? equiv * factor : equiv;
            })
            .filter(val -> val > 0)
            .findFirst();

        if (factorGenerico.isPresent()) {
            double resultado = gramos / factorGenerico.get();
            System.out.printf("🔄 Gramos a herramienta genérica: %.4f / %.4f = %.4f %s%n", 
                             gramos, factorGenerico.get(), resultado, unidadDestino);
            return resultado;
        }

        return null;
    }

    private static Double buscarEnUnidadesPeso(String unidad) {
        return UNIDADES_PESO.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(unidad))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private static Double buscarEnUnidadesVolumen(String unidad) {
        return UNIDADES_VOLUMEN.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(unidad))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private static Double buscarEnUnidadesEstandar(String unidad) {
        return UNIDADES_ESTANDAR.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(unidad))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    private static boolean esUnidadDePeso(String unidad) {
        return UNIDADES_PESO.keySet().stream().anyMatch(u -> u.equalsIgnoreCase(unidad));
    }
    
    private static boolean esUnidadDeVolumen(String unidad) {
        return UNIDADES_VOLUMEN.keySet().stream().anyMatch(u -> u.equalsIgnoreCase(unidad));
    }

    private static double extraerNumero(String entrada) {
        try {
            if (entrada == null || entrada.trim().isEmpty()) return 0;
            
            // Limpiar la cadena: mantener solo números, puntos y comas
            String limpio = entrada.replace(",", ".").replaceAll("[^0-9.]", "").trim();
            
            if (limpio.isEmpty()) return 0;
            
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Error al parsear número: " + entrada);
            return 0;
        }
    }

    private static List<Map<String, String>> obtenerDatos() {
        if (CACHE_DATOS == null) {
            CACHE_DATOS = VerUtils.verTabla("TabladeConversión");
            System.out.println("📥 [ConversorUtils2] Datos de conversión obtenidos: " + CACHE_DATOS.size() + " filas.");
        }
        return CACHE_DATOS;
    }
    
    // ✅ Método para limpiar cache cuando se actualicen los datos
    public static void limpiarCache() {
        CACHE_DATOS = null;
        CACHE_EQUIVALENCIAS.clear();
        System.out.println("🧹 Cache de conversiones limpiado");
    }
}