package com.panaderiafx.utils;

import java.util.*;

public class ConversorUtils2 {
    
    private static final String TABLA = "TabladeConversión";
    private static List<Map<String, String>> CACHE_TABLA;
    private static final int MAX_CONVERSION_ATTEMPTS = 3; // Evitar loops infinitos

    public static Double convertir(String tipoLogico, String unidadOrigen, String unidadDestino, double cantidad, String ingrediente) {
        return convertir(tipoLogico, unidadOrigen, unidadDestino, cantidad, ingrediente, 0);
    }

    private static Double convertir(String tipoLogico, String unidadOrigen, String unidadDestino, double cantidad, String ingrediente, int attempts) {
        if (attempts >= MAX_CONVERSION_ATTEMPTS) {
            System.out.printf("🛑 Máximo de intentos alcanzado (%d) para evitar loop infinito%n", MAX_CONVERSION_ATTEMPTS);
            return null;
        }

        if (unidadOrigen.equalsIgnoreCase(unidadDestino)) {
            System.out.printf("⚠️ Unidades iguales [%s], retorno directo: %.2f%n", unidadOrigen, cantidad);
            return cantidad;
        }

        // 1. Intentar conversión con mapas estáticos primero (más rápido)
        Double resultadoEstatico = intentarConversionEstatica(unidadOrigen, unidadDestino, cantidad, ingrediente);
        if (resultadoEstatico != null) {
            return resultadoEstatico;
        }

        // 2. Si falla, intentar conversión inteligente vía Gramos
        Double intermedio = null;
        if (attempts == 0) { // Solo en el primer intento para evitar loops
            intermedio = ConversorInteligenteUtils.convertir(unidadOrigen, "Gramos", cantidad, ingrediente);
        }
        
        if (intermedio == null) {
            System.out.printf("❌ Conversión a Gramos falló: %s → Gramos%n", unidadOrigen);
            // 3. Intentar conversión directa con tabla Excel
            return intentarConversionDirectaConTabla(unidadOrigen, unidadDestino, cantidad, ingrediente);
        }

        System.out.printf("🔧 Conversor establecido toma el control: %.4f Gramos → %s%n", intermedio, unidadDestino);

        // 4. Intentar conversión desde Gramos usando mapas estáticos
        Double resultadoFinal = convertirDesdeGramos(intermedio, unidadDestino, ingrediente);
        if (resultadoFinal != null) {
            return resultadoFinal;
        }

        // 5. Intentar conversión desde Gramos usando tabla Excel
        Double resultadoTabla = convertirDesdeGramosConTabla(intermedio, unidadDestino, ingrediente);
        if (resultadoTabla != null) {
            return resultadoTabla;
        }

        // 6. Último recurso: conversión inteligente inversa (solo si no hemos intentado antes)
        if (attempts == 0) {
            System.out.printf("🔁 Último intento: conversión inversa Gramos → %s usando ConversorInteligenteUtils%n", unidadDestino);
            return convertir(tipoLogico, "Gramos", unidadDestino, intermedio, ingrediente, attempts + 1);
        }

        System.out.printf("❌ Todas las estrategias de conversión fallaron%n");
        return null;
    }

    private static Double intentarConversionEstatica(String unidadOrigen, String unidadDestino, double cantidad, String ingrediente) {
        // Conversión directa entre unidades conocidas
        if (UNIDADES_PESO.containsKey(unidadOrigen) && UNIDADES_PESO.containsKey(unidadDestino)) {
            double gramos = cantidad * UNIDADES_PESO.get(unidadOrigen);
            double resultado = gramos / UNIDADES_PESO.get(unidadDestino);
            System.out.printf("⚖️ %.4f %s = %.4f g = %.4f %s%n", cantidad, unidadOrigen, gramos, resultado, unidadDestino);
            return resultado;
        }

        if (UNIDADES_VOLUMEN.containsKey(unidadOrigen) && UNIDADES_VOLUMEN.containsKey(unidadDestino)) {
            double ml = cantidad * UNIDADES_VOLUMEN.get(unidadOrigen);
            double resultado = ml / UNIDADES_VOLUMEN.get(unidadDestino);
            System.out.printf("💧 %.4f %s = %.4f ml = %.4f %s%n", cantidad, unidadOrigen, ml, resultado, unidadDestino);
            return resultado;
        }

        return null;
    }

    private static Double intentarConversionDirectaConTabla(String unidadOrigen, String unidadDestino, double cantidad, String ingrediente) {
        System.out.printf("📊 Buscando conversión directa en tabla: %s → %s%n", unidadOrigen, unidadDestino);
        
        List<Map<String, String>> tabla = getTabla();
        
        // Buscar conversión directa
        Optional<Map<String, String>> conversion = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(unidadOrigen) && 
                           d.get("Unidad 2").equalsIgnoreCase(unidadDestino))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (conversion.isPresent()) {
            double factor = extraerNumero(conversion.get().get("Equivalencia aproximada"));
            double resultado = cantidad * factor;
            System.out.printf("📊 %.4f %s * %.4f = %.4f %s%n", cantidad, unidadOrigen, factor, resultado, unidadDestino);
            return resultado;
        }

        // Buscar conversión inversa
        Optional<Map<String, String>> conversionInversa = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(unidadDestino) && 
                           d.get("Unidad 2").equalsIgnoreCase(unidadOrigen))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (conversionInversa.isPresent()) {
            double factor = extraerNumero(conversionInversa.get().get("Equivalencia aproximada"));
            double resultado = cantidad / factor;
            System.out.printf("📊 %.4f %s ÷ %.4f = %.4f %s (conversión inversa)%n", cantidad, unidadOrigen, factor, resultado, unidadDestino);
            return resultado;
        }

        return null;
    }

    private static Double convertirDesdeGramos(double gramos, String unidadDestino, String ingrediente) {
        if (unidadDestino.equalsIgnoreCase("Gramos") || unidadDestino.equalsIgnoreCase("g")) return gramos;

        if (UNIDADES_PESO.containsKey(unidadDestino)) {
            double resultado = gramos / UNIDADES_PESO.get(unidadDestino);
            System.out.printf("⚖️ %.4f g ÷ %.4f = %.4f %s%n", gramos, UNIDADES_PESO.get(unidadDestino), resultado, unidadDestino);
            return resultado;
        }

        if (UNIDADES_VOLUMEN.containsKey(unidadDestino)) {
            double ml = gramos / FACTOR_AGUA_PESO_VOLUMEN;
            double resultado = ml / UNIDADES_VOLUMEN.get(unidadDestino);
            System.out.printf("💧 %.4f g (agua) = %.4f ml → %.4f %s%n", gramos, ml, resultado, unidadDestino);
            return resultado;
        }

        return null;
    }

    private static Double convertirDesdeGramosConTabla(double gramos, String unidadDestino, String ingrediente) {
        System.out.printf("📊 Buscando conversión desde Gramos → %s en tabla%n", unidadDestino);
        
        List<Map<String, String>> tabla = getTabla();
        
        // 1. Buscar conversión directa desde Gramos
        Optional<Map<String, String>> conversion = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase("Gramos") && 
                           d.get("Unidad 2").equalsIgnoreCase(unidadDestino))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (conversion.isPresent()) {
            double factor = extraerNumero(conversion.get().get("Equivalencia aproximada"));
            double resultado = gramos * factor;
            System.out.printf("📊 %.4f Gramos * %.4f = %.4f %s%n", gramos, factor, resultado, unidadDestino);
            return resultado;
        }

        // 2. Buscar conversión inversa hacia Gramos
        Optional<Map<String, String>> conversionInversa = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(unidadDestino) && 
                           d.get("Unidad 2").equalsIgnoreCase("Gramos"))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (conversionInversa.isPresent()) {
            double factor = extraerNumero(conversionInversa.get().get("Equivalencia aproximada"));
            double resultado = gramos / factor;
            System.out.printf("📊 %.4f Gramos ÷ %.4f = %.4f %s (conversión inversa)%n", gramos, factor, resultado, unidadDestino);
            return resultado;
        }

        // 3. Buscar conversión indirecta: Gramos → Volumen → Herramienta
        Double resultadoIndirecto = intentarConversionIndirectaGramosAHerramienta(gramos, unidadDestino, ingrediente, tabla);
        if (resultadoIndirecto != null) {
            return resultadoIndirecto;
        }

        // 4. Buscar conversión indirecta: Gramos → Peso → Herramienta
        return intentarConversionIndirectaGramosAPeso(gramos, unidadDestino, ingrediente, tabla);
    }

    private static Double intentarConversionIndirectaGramosAHerramienta(double gramos, String unidadDestino, String ingrediente, List<Map<String, String>> tabla) {
        // Buscar herramientas que tengan volumen como unidad 2
        Optional<Map<String, String>> herramientaVolumen = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(unidadDestino))
                .filter(d -> esUnidadDeVolumen(d.get("Unidad 2")))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (herramientaVolumen.isPresent()) {
            String unidadVolumen = herramientaVolumen.get().get("Unidad 2");
            double factorHerramienta = extraerNumero(herramientaVolumen.get().get("Equivalencia aproximada"));
            
            System.out.printf("🔧 Encontrada herramienta via volumen: 1 %s = %.2f %s%n", 
                    unidadDestino, factorHerramienta, unidadVolumen);

            // Convertir Gramos → Volumen usando densidad específica
            Double volumen = convertirGramosAVolumen(gramos, unidadVolumen, ingrediente, tabla);
            if (volumen != null) {
                // Convertir Volumen → Herramienta
                double resultado = volumen / factorHerramienta;
                System.out.printf("🏺 %.4f %s ÷ %.4f = %.4f %s%n", 
                        volumen, unidadVolumen, factorHerramienta, resultado, unidadDestino);
                return resultado;
            }
        }

        return null;
    }

    private static Double intentarConversionIndirectaGramosAPeso(double gramos, String unidadDestino, String ingrediente, List<Map<String, String>> tabla) {
        // Buscar herramientas que tengan peso como unidad 2
        Optional<Map<String, String>> herramientaPeso = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(unidadDestino))
                .filter(d -> esUnidadDePeso(d.get("Unidad 2")))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (herramientaPeso.isPresent()) {
            String unidadPeso = herramientaPeso.get().get("Unidad 2");
            double factorHerramienta = extraerNumero(herramientaPeso.get().get("Equivalencia aproximada"));
            
            System.out.printf("🔧 Encontrada herramienta via peso: 1 %s = %.2f %s%n", 
                    unidadDestino, factorHerramienta, unidadPeso);

            // Convertir Gramos → Unidad de peso
            Double peso = convertirGramosAPeso(gramos, unidadPeso);
            if (peso != null) {
                // Convertir Peso → Herramienta
                double resultado = peso / factorHerramienta;
                System.out.printf("⚖️ %.4f %s ÷ %.4f = %.4f %s%n", 
                        peso, unidadPeso, factorHerramienta, resultado, unidadDestino);
                return resultado;
            }
        }

        return null;
    }

    private static Double convertirGramosAVolumen(double gramos, String unidadVolumen, String ingrediente, List<Map<String, String>> tabla) {
        // Buscar densidad específica del ingrediente
        Optional<Map<String, String>> densidad = tabla.stream()
                .filter(d -> d.get("Unidad base").equalsIgnoreCase("Mililitros"))
                .filter(d -> d.get("Unidad 2").equalsIgnoreCase("Gramos"))
                .filter(d -> ingrediente != null && ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .findFirst();

        double ml;
        if (densidad.isPresent()) {
            double factor = extraerNumero(densidad.get().get("Equivalencia aproximada"));
            ml = gramos / factor; // Gramos ÷ (g/ml) = ml
            System.out.printf("🧪 %.2f g ÷ %.4f (densidad específica) = %.4f ml%n", gramos, factor, ml);
        } else {
            // Fallback: densidad del agua
            ml = gramos / 1.0;
            System.out.printf("💧 %.2f g ÷ 1.0 (fallback agua) = %.4f ml%n", gramos, ml);
        }

        // Convertir ml a la unidad de volumen deseada
        if (UNIDADES_VOLUMEN.containsKey(unidadVolumen)) {
            double factorVolumen = UNIDADES_VOLUMEN.get(unidadVolumen);
            double resultado = ml / factorVolumen;
            System.out.printf("🔄 %.2f ml ÷ %.4f = %.4f %s%n", ml, factorVolumen, resultado, unidadVolumen);
            return resultado;
        }

        return null;
    }

    private static Double convertirGramosAPeso(double gramos, String unidadPeso) {
        if (UNIDADES_PESO.containsKey(unidadPeso)) {
            double factorPeso = UNIDADES_PESO.get(unidadPeso);
            double resultado = gramos / factorPeso;
            System.out.printf("⚖️ %.2f g ÷ %.4f = %.4f %s%n", gramos, factorPeso, resultado, unidadPeso);
            return resultado;
        }
        return null;
    }

    private static boolean esUnidadDeVolumen(String unidad) {
        return UNIDADES_VOLUMEN.containsKey(unidad) || 
               Set.of("Litros", "Litro", "Mililitros", "ml", "Onza líquida").stream()
                   .anyMatch(u -> u.equalsIgnoreCase(unidad));
    }

    private static boolean esUnidadDePeso(String unidad) {
        return UNIDADES_PESO.containsKey(unidad) || 
               Set.of("Libras", "Libra", "Kilos", "Kilo", "Gramos", "g", "Onzas", "Onza").stream()
                   .anyMatch(u -> u.equalsIgnoreCase(unidad));
    }

    private static List<Map<String, String>> getTabla() {
        if (CACHE_TABLA == null) {
            CACHE_TABLA = VerUtils.verTabla(TABLA);
            System.out.println("📥 [ConversorUtils2] Tabla cargada con " + CACHE_TABLA.size() + " filas.");
        }
        return CACHE_TABLA;
    }

    private static double extraerNumero(String texto) {
        try {
            if (texto == null || texto.trim().isEmpty()) return 0;
            String limpio = texto.replace(",", ".").replaceAll("[^0-9.]", "");
            return limpio.isEmpty() ? 0 : Double.parseDouble(limpio);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void limpiarCache() {
        CACHE_TABLA = null;
        System.out.println("🧹 Cache del ConversorUtils2 limpiado.");
    }

    private static final Map<String, Double> UNIDADES_PESO = Map.ofEntries(
            Map.entry("Kilos", 1000.0),
            Map.entry("Kilo", 1000.0),
            Map.entry("Gramos", 1.0),
            Map.entry("g", 1.0),
            Map.entry("Libras", 453.592),
            Map.entry("Libra", 453.592),
            Map.entry("Onzas", 28.3495),
            Map.entry("Onza", 28.3495)
    );

    private static final Map<String, Double> UNIDADES_VOLUMEN = Map.ofEntries(
            Map.entry("Litro", 1000.0),
            Map.entry("Litros", 1000.0),
            Map.entry("Mililitros", 1.0),
            Map.entry("ml", 1.0),
            Map.entry("Onza líquida", 29.5735),
            Map.entry("Onza", 29.5735)
    );

    private static final double FACTOR_AGUA_PESO_VOLUMEN = 1.0;
}