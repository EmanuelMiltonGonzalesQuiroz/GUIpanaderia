package com.panaderiafx.utils;

import java.util.*;

public class ConversorUtils2 {

    private static final String TABLA = "TabladeConversión";

    private static final Map<String, Double> UNIDADES_ESTANDAR = Map.ofEntries(
            Map.entry("Kilos", 1000.0),
            Map.entry("Gramos", 1.0),
            Map.entry("Libras", 453.592),
            Map.entry("Onzas", 28.3495),
            Map.entry("Litro", 1000.0),
            Map.entry("Mililitros", 1.0),
            Map.entry("Onza", 29.5735)
    );

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

        List<Map<String, String>> datos = obtenerDatos();

        List<String> tiposPermitidos = switch (tipoLogico) {
            case "Herramienta" -> List.of("Herramienta", "Peso", "Volumen");
            case "Peso" -> List.of("Peso", "Herramienta");
            case "Volumen" -> List.of("Volumen", "Herramienta");
            default -> List.of(tipoLogico);
        };

        Double base = convertirAUnidadBase(cantidad, unidadOrigen, ingrediente, tiposPermitidos, datos);
        if (base == null) return null;

        Double divisor = equivalenciaParaUnidad(unidadDestino, ingrediente, tiposPermitidos, datos);
        if (divisor == null || divisor <= 0) return null;

        return base / divisor;
    }

    private static Double convertirAUnidadBase(double cantidad,
                                               String unidad,
                                               String ingrediente,
                                               List<String> tiposPermitidos,
                                               List<Map<String, String>> datos) {
        Double factor = equivalenciaParaUnidad(unidad, ingrediente, tiposPermitidos, datos);
        if (factor == null || factor <= 0) return null;

        return cantidad * factor;
    }

    private static Double equivalenciaParaUnidad(String unidad,
                                                 String ingrediente,
                                                 List<String> tiposPermitidos,
                                                 List<Map<String, String>> datos) {
        String clave = (ingrediente == null ? "" : ingrediente.trim()) + "|" + unidad;
        if (CACHE_EQUIVALENCIAS.containsKey(clave)) {
            return CACHE_EQUIVALENCIAS.get(clave);
        }

        if (UNIDADES_ESTANDAR.containsKey(unidad)) {
            Double val = UNIDADES_ESTANDAR.get(unidad);
            CACHE_EQUIVALENCIAS.put(clave, val);
            return val;
        }

        Optional<Double> directa = datos.stream()
                .filter(d -> tiposPermitidos.contains(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ingrediente == null || ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> extraerNumero(d.get("Equivalencia aproximada")))
                .filter(val -> val > 0)
                .findFirst();

        if (directa.isPresent()) {
            CACHE_EQUIVALENCIAS.put(clave, directa.get());
            return directa.get();
        }

        Optional<Double> inversa = datos.stream()
                .filter(d -> tiposPermitidos.contains(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad 2")))
                .filter(d -> ingrediente == null || ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "").trim()))
                .map(d -> extraerNumero(d.get("Equivalencia aproximada")))
                .filter(val -> val > 0)
                .map(val -> 1 / val)
                .findFirst();

        if (inversa.isPresent()) {
            CACHE_EQUIVALENCIAS.put(clave, inversa.get());
            return inversa.get();
        }

        return null;
    }

    private static double extraerNumero(String entrada) {
        try {
            if (entrada == null) return 0;
            return Double.parseDouble(entrada.replace(",", ".").replaceAll("[^0-9.]", "").trim());
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Error al parsear número: " + entrada);
            return 0;
        }
    }

    private static List<Map<String, String>> obtenerDatos() {
        if (CACHE_DATOS == null) {
            CACHE_DATOS = VerUtils.verTabla("TabladeConversión"); // ✅ usa caché global precargada
            System.out.println("📥 [ConversorUtils2] Datos de conversión obtenidos: " + CACHE_DATOS.size() + " filas.");
        }
        return CACHE_DATOS;
    }
}
