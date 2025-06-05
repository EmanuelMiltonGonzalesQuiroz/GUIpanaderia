package com.panaderiafx.utils;

import java.util.*;
import java.util.stream.Collectors;

public class ConversorInteligenteUtils {

    private static final String TABLA = "TabladeConversión";
    private static List<Map<String, String>> CACHE_TABLA;
    private static final int MAX_DEPTH = 10; // Evitar recursión infinita

    public static Double convertir(String unidadOrigen, String unidadDestino, double cantidad, String ingrediente) {
        return convertir(unidadOrigen, unidadDestino, cantidad, ingrediente, 0);
    }

    private static Double convertir(String unidadOrigen, String unidadDestino, double cantidad, String ingrediente, int depth) {
        if (depth > MAX_DEPTH) {
            System.out.println("❌ Máxima profundidad de recursión alcanzada");
            return null;
        }

        String origenNormalizado = normalizarUnidad(unidadOrigen);
        String destinoNormalizado = normalizarUnidad(unidadDestino);

        System.out.printf("🨠 Conversor Inteligente (depth %d): %.2f %s → %s (Ingrediente: %s)%n",
                depth, cantidad, origenNormalizado, destinoNormalizado, ingrediente);

        if (origenNormalizado.equalsIgnoreCase(destinoNormalizado)) return cantidad;

        List<Map<String, String>> tabla = getTabla();
        List<NodoConversion> pasos = buscarRutaConversion(tabla, origenNormalizado, destinoNormalizado, ingrediente);

        if (pasos == null) {
            // Intento 1: Conversión inversa
            pasos = buscarRutaConversion(tabla, destinoNormalizado, origenNormalizado, ingrediente);
            if (pasos != null) {
                return aplicarConversionInversa(cantidad, pasos, destinoNormalizado);
            }

            // Intento 2: Conversión con paso intermedio recursivo
            Double resultadoIntermedio = buscarConversionIntermedia(tabla, origenNormalizado, destinoNormalizado, cantidad, ingrediente, depth);
            if (resultadoIntermedio != null) {
                return resultadoIntermedio;
            }

            // Intento 3: Conversión por densidad (volumen → peso)
            Double resultadoDensidad = intentarConversionPorDensidad(tabla, origenNormalizado, destinoNormalizado, cantidad, ingrediente);
            if (resultadoDensidad != null) {
                return resultadoDensidad;
            }

            // Intento 4: Conversión por densidad inversa (peso → volumen)
            Double resultadoDensidadInversa = intentarConversionPorDensidadInversa(tabla, origenNormalizado, destinoNormalizado, cantidad, ingrediente);
            if (resultadoDensidadInversa != null) {
                return resultadoDensidadInversa;
            }

            System.out.println("❌ No se pudo encontrar una ruta de conversión.");
            return null;
        }

        return aplicarConversionDirecta(cantidad, pasos, destinoNormalizado);
    }

    private static List<NodoConversion> buscarRutaConversion(List<Map<String, String>> tabla,
                                                              String unidadInicio,
                                                              String unidadDestino,
                                                              String ingrediente) {

        Queue<NodoConversion> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();

        cola.add(new NodoConversion(unidadInicio, unidadDestino, 1.0, null, 1.0));

        while (!cola.isEmpty()) {
            NodoConversion actual = cola.poll();
            String claveVisitado = actual.origenUnidad.toLowerCase();

            if (visitados.contains(claveVisitado)) continue;
            visitados.add(claveVisitado);

            if (actual.origenUnidad.equalsIgnoreCase(unidadDestino)) {
                return reconstruirRuta(actual);
            }

            // Buscar todas las conversiones posibles desde la unidad actual
            List<Map<String, String>> relacionados = buscarConversionesRelacionadas(tabla, actual.origenUnidad, ingrediente);

            for (Map<String, String> fila : relacionados) {
                String unidadBase = fila.get("Unidad base").trim();
                String unidad2 = fila.get("Unidad 2").trim();
                double factor = extraerNumero(fila.get("Equivalencia aproximada"));
                if (factor <= 0) continue;

                // Conversión directa: unidadBase → unidad2
                if (unidadBase.equalsIgnoreCase(actual.origenUnidad)) {
                    String siguienteUnidad = unidad2;
                    double nuevoFactor = actual.factorTotal * factor;
                    cola.add(new NodoConversion(siguienteUnidad, unidadDestino, nuevoFactor, actual, factor));
                }

                // Conversión inversa: unidad2 → unidadBase
                if (unidad2.equalsIgnoreCase(actual.origenUnidad)) {
                    String siguienteUnidad = unidadBase;
                    double nuevoFactor = actual.factorTotal / factor;
                    cola.add(new NodoConversion(siguienteUnidad, unidadDestino, nuevoFactor, actual, 1.0 / factor));
                }
            }
        }

        return null;
    }

    private static List<Map<String, String>> buscarConversionesRelacionadas(List<Map<String, String>> tabla, String unidad, String ingrediente) {
        return tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> {
                    String base = d.get("Unidad base").trim();
                    String unidad2 = d.get("Unidad 2").trim();
                    return base.equalsIgnoreCase(unidad) || unidad2.equalsIgnoreCase(unidad);
                })
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    String tipo = d.getOrDefault("Tipo de medida", "").trim();
                    
                    // Para herramientas, ser más flexible con el ingrediente
                    if ("Herramienta".equalsIgnoreCase(tipo)) {
                        return ing.isEmpty() || "—".equals(ing) || 
                               (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                    }
                    
                    // Para conversiones normales, ser más estricto
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .collect(Collectors.toList());
    }

    private static Double aplicarConversionInversa(double cantidad, List<NodoConversion> pasos, String destinoNormalizado) {
        double resultado = cantidad;
        for (int i = pasos.size() - 1; i >= 0; i--) {
            NodoConversion paso = pasos.get(i);
            resultado /= paso.factor;
            System.out.printf("⬅️ %.4f %s ← %.4f %s (1/%.4f)%n",
                    resultado, paso.origenUnidad, cantidad, paso.destinoUnidad, paso.factor);
            cantidad = resultado;
        }
        System.out.printf("✅ Conversión inversa final: %.4f %s%n", resultado, destinoNormalizado);
        return resultado;
    }

    private static Double aplicarConversionDirecta(double cantidad, List<NodoConversion> pasos, String destinoNormalizado) {
        double resultado = cantidad;
        for (NodoConversion paso : pasos) {
            resultado *= paso.factor;
            System.out.printf("➡️ %.4f %s → %.4f %s (%.4f)%n",
                    cantidad, paso.origenUnidad, resultado, paso.destinoUnidad, paso.factor);
            cantidad = resultado;
        }
        System.out.printf("✅ Conversión final: %.4f %s%n", resultado, destinoNormalizado);
        return resultado;
    }

    private static Double buscarConversionIntermedia(List<Map<String, String>> tabla, String origenNormalizado, 
                                                    String destinoNormalizado, double cantidad, String ingrediente, int depth) {
        // Buscar una unidad intermedia desde el origen
        Optional<Map<String, String>> intermedia = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(origenNormalizado))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    String tipo = d.getOrDefault("Tipo de medida", "").trim();
                    
                    // Para herramientas, ser más flexible
                    if ("Herramienta".equalsIgnoreCase(tipo)) {
                        return ing.isEmpty() || "—".equals(ing) || 
                               (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                    }
                    
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (intermedia.isPresent()) {
            String interUnidad = intermedia.get().get("Unidad 2");
            double factor = extraerNumero(intermedia.get().get("Equivalencia aproximada"));
            double intermedioValor = cantidad * factor;

            System.out.printf("↪️ Paso intermedio: %.2f %s = %.2f %s%n", 
                    cantidad, origenNormalizado, intermedioValor, interUnidad);

            return convertir(interUnidad, destinoNormalizado, intermedioValor, ingrediente, depth + 1);
        }

        // También buscar desde el destino hacia el origen
        Optional<Map<String, String>> intermediaInversa = tabla.stream()
                .filter(d -> d.get("Unidad base") != null && d.get("Unidad 2") != null)
                .filter(d -> d.get("Unidad base").equalsIgnoreCase(destinoNormalizado))
                .filter(d -> {
                    String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    String tipo = d.getOrDefault("Tipo de medida", "").trim();
                    
                    if ("Herramienta".equalsIgnoreCase(tipo)) {
                        return ing.isEmpty() || "—".equals(ing) || 
                               (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                    }
                    
                    return ing.isEmpty() || "—".equals(ing) || 
                           (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                })
                .findFirst();

        if (intermediaInversa.isPresent()) {
            String interUnidad = intermediaInversa.get().get("Unidad 2");
            double factor = extraerNumero(intermediaInversa.get().get("Equivalencia aproximada"));
            
            System.out.printf("🔄 Probando ruta inversa: %s → %s → %s%n", 
                    origenNormalizado, interUnidad, destinoNormalizado);

            // Convertir origen → intermedia
            Double valorIntermedio = convertir(origenNormalizado, interUnidad, cantidad, ingrediente, depth + 1);
            if (valorIntermedio != null) {
                // Convertir intermedia → destino (inversión del factor)
                double resultado = valorIntermedio / factor;
                System.out.printf("↩️ Paso final inverso: %.2f %s ÷ %.2f = %.2f %s%n", 
                        valorIntermedio, interUnidad, factor, resultado, destinoNormalizado);
                return resultado;
            }
        }

        return null;
    }

    private static Double intentarConversionPorDensidad(List<Map<String, String>> tabla, String origenNormalizado, 
                                                       String destinoNormalizado, double cantidad, String ingrediente) {
        if (esUnidadDeVolumen(origenNormalizado) && destinoNormalizado.equalsIgnoreCase("Gramos")) {
            // Convertir a mililitros primero
            double ml = cantidad * buscarFactorVolumen(origenNormalizado);

            // Buscar densidad específica del ingrediente
            Optional<Map<String, String>> densidad = tabla.stream()
                    .filter(d -> d.get("Unidad base").equalsIgnoreCase("Mililitros"))
                    .filter(d -> d.get("Unidad 2").equalsIgnoreCase("Gramos"))
                    .filter(d -> ingrediente != null && ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                    .findFirst();

            if (densidad.isPresent()) {
                double factor = extraerNumero(densidad.get().get("Equivalencia aproximada"));
                double gramos = ml * factor;
                System.out.printf("🧪 %.2f ml * %.4f (densidad específica) = %.4f g%n", ml, factor, gramos);
                return gramos;
            } else {
                // Fallback: densidad del agua
                double gramos = ml * 1.0;
                System.out.printf("💧 %.2f ml * 1.0 (fallback agua) = %.4f g%n", ml, gramos);
                return gramos;
            }
        }
        return null;
    }

    private static Double intentarConversionPorDensidadInversa(List<Map<String, String>> tabla, String origenNormalizado, 
                                                             String destinoNormalizado, double cantidad, String ingrediente) {
        if (origenNormalizado.equalsIgnoreCase("Gramos")) {
            // Buscar conversión a herramientas que usan volumen (como Caja)
            Optional<Map<String, String>> herramientaVolumen = tabla.stream()
                    .filter(d -> d.get("Unidad 2") != null && esUnidadDeVolumen(d.get("Unidad 2")))
                    .filter(d -> d.get("Unidad base") != null && d.get("Unidad base").equalsIgnoreCase(destinoNormalizado))
                    .filter(d -> {
                        String ing = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                        return ing.isEmpty() || "—".equals(ing) || 
                               (ingrediente != null && ingrediente.equalsIgnoreCase(ing));
                    })
                    .findFirst();

            if (herramientaVolumen.isPresent()) {
                String unidadVolumen = herramientaVolumen.get().get("Unidad 2");
                double factorHerramienta = extraerNumero(herramientaVolumen.get().get("Equivalencia aproximada"));
                
                System.out.printf("🔧 Encontrada herramienta: 1 %s = %.2f %s%n", 
                        destinoNormalizado, factorHerramienta, unidadVolumen);

                // Convertir Gramos → Volumen usando densidad
                Double volumenResult = intentarConversionPorDensidadInversa_Simple(cantidad, unidadVolumen, ingrediente, tabla);
                if (volumenResult != null) {
                    // Convertir Volumen → Herramienta
                    double resultadoFinal = volumenResult / factorHerramienta;
                    System.out.printf("🏺 %.4f %s ÷ %.4f = %.4f %s%n", 
                            volumenResult, unidadVolumen, factorHerramienta, resultadoFinal, destinoNormalizado);
                    return resultadoFinal;
                }
            }

            // Conversión normal de peso a volumen
            if (esUnidadDeVolumen(destinoNormalizado)) {
                return intentarConversionPorDensidadInversa_Simple(cantidad, destinoNormalizado, ingrediente, tabla);
            }
        }
        return null;
    }

    private static Double intentarConversionPorDensidadInversa_Simple(double cantidadGramos, String unidadVolumenDestino, 
                                                                     String ingrediente, List<Map<String, String>> tabla) {
        // Buscar densidad específica del ingrediente
        Optional<Map<String, String>> densidad = tabla.stream()
                .filter(d -> d.get("Unidad base").equalsIgnoreCase("Mililitros"))
                .filter(d -> d.get("Unidad 2").equalsIgnoreCase("Gramos"))
                .filter(d -> ingrediente != null && ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .findFirst();

        double ml;
        if (densidad.isPresent()) {
            double factor = extraerNumero(densidad.get().get("Equivalencia aproximada"));
            ml = cantidadGramos / factor; // Gramos / (g/ml) = ml
            System.out.printf("🧪 %.2f g ÷ %.4f (densidad específica) = %.4f ml%n", cantidadGramos, factor, ml);
        } else {
            // Fallback: densidad del agua
            ml = cantidadGramos / 1.0;
            System.out.printf("💧 %.2f g ÷ 1.0 (fallback agua) = %.4f ml%n", cantidadGramos, ml);
        }

        // Convertir de mililitros a la unidad de volumen deseada
        double factorVolumen = buscarFactorVolumen(unidadVolumenDestino);
        if (factorVolumen > 0) {
            double resultado = ml / factorVolumen;
            System.out.printf("🔄 %.2f ml ÷ %.4f = %.4f %s%n", ml, factorVolumen, resultado, unidadVolumenDestino);
            return resultado;
        }

        return null;
    }

    private static List<NodoConversion> reconstruirRuta(NodoConversion nodoFinal) {
        List<NodoConversion> ruta = new ArrayList<>();
        NodoConversion actual = nodoFinal;
        while (actual != null && actual.anterior != null) {
            ruta.add(0, actual);
            actual = actual.anterior;
        }
        return ruta;
    }

    private static List<Map<String, String>> getTabla() {
        if (CACHE_TABLA == null) {
            CACHE_TABLA = VerUtils.verTabla(TABLA);
            System.out.println("📥 [ConversorInteligente] Tabla cargada con " + CACHE_TABLA.size() + " filas.");
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

    private static boolean esUnidadDeVolumen(String unidad) {
        return Set.of("Litro", "Litros", "Mililitros", "ml", "Onza", "Onza líquida").stream()
                .anyMatch(u -> u.equalsIgnoreCase(unidad));
    }

    private static double buscarFactorVolumen(String unidad) {
        return switch (unidad.toLowerCase()) {
            case "litro", "litros" -> 1000.0;
            case "mililitros", "ml" -> 1.0;
            case "onza", "onza líquida" -> 29.5735;
            default -> 0.0;
        };
    }

    private static String normalizarUnidad(String unidad) {
        return switch (unidad.toLowerCase()) {
            case "g" -> "Gramos";
            case "kg" -> "Kilo";
            case "l" -> "Litro";
            case "ml" -> "Mililitros";
            default -> unidad;
        };
    }

    private static class NodoConversion {
        String origenUnidad;
        String destinoUnidad;
        double origenCantidad;
        NodoConversion anterior;
        double factor;
        double factorTotal;

        public NodoConversion(String origenUnidad, String destinoUnidad, double factorTotal, NodoConversion anterior, double factor) {
            this.origenUnidad = origenUnidad;
            this.destinoUnidad = destinoUnidad;
            this.anterior = anterior;
            this.factor = factor;
            this.factorTotal = factorTotal;
            this.origenCantidad = factorTotal;
        }
    }

    public static void limpiarCache() {
        CACHE_TABLA = null;
        System.out.println("🧹 Cache del conversor inteligente limpiado.");
    }
}