package com.panaderiafx.utils;

import java.util.*;

public class ConversorUtils {

    private static final String TABLA = "TabladeConversión";

    // Unidades estándar (valor en gramos o mililitros)
    private static final Map<String, Double> UNIDADES_ESTANDAR = Map.ofEntries(
            Map.entry("Kilos", 1000.0),
            Map.entry("Gramos", 1.0),
            Map.entry("Libras", 453.592),
            Map.entry("Onzas", 28.3495),
            Map.entry("Litro", 1000.0),
            Map.entry("Mililitros", 1.0),
            Map.entry("Onza", 29.5735) // Onza líquida
    );

    public static Double convertir(String tipoLogico,
                                   String unidadOrigen,
                                   String unidadDestino,
                                   double cantidad,
                                   String ingrediente) {

        List<Map<String, String>> datos = VerUtils.verTabla(TABLA);

        List<String> tiposPermitidos = switch (tipoLogico) {
            case "Herramienta" -> List.of("Herramienta", "Peso", "Volumen");
            case "Peso" -> List.of("Peso", "Herramienta");
            case "Volumen" -> List.of("Volumen", "Herramienta");
            default -> List.of(tipoLogico);
        };

        // === Paso 1: convertir a gramos o mililitros ===
        Double base = convertirAUnidadBase(cantidad, unidadOrigen, ingrediente, tiposPermitidos, datos);
        if (base == null) return null;

        // === Paso 2: convertir desde gramos/mL a unidad destino ===
        Double divisor = equivalenciaParaUnidad(unidadDestino, ingrediente, tiposPermitidos, datos);
        if (divisor == null || divisor <= 0) return null;

        return base / divisor;
    }

    // 🔹 Obtener valor en gramos o mililitros
    private static Double convertirAUnidadBase(double cantidad,
                                               String unidad,
                                               String ingrediente,
                                               List<String> tiposPermitidos,
                                               List<Map<String, String>> datos) {

        Double factor = equivalenciaParaUnidad(unidad, ingrediente, tiposPermitidos, datos);
        if (factor == null || factor <= 0) return null;

        return cantidad * factor;
    }

    // 🔹 Encuentra la equivalencia para una unidad (puede ser estándar o desde tabla)
    private static Double equivalenciaParaUnidad(String unidad,
                                                 String ingrediente,
                                                 List<String> tiposPermitidos,
                                                 List<Map<String, String>> datos) {

        // Si es unidad estándar (Kilos, Gramos, etc.)
        if (UNIDADES_ESTANDAR.containsKey(unidad)) return UNIDADES_ESTANDAR.get(unidad);

        // Si está en la tabla, buscar primero con ingrediente
        Optional<Map<String, String>> conIngrediente = datos.stream()
                .filter(d -> tiposPermitidos.contains(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> ingrediente != null && ingrediente.equalsIgnoreCase(d.getOrDefault("Ingrediente (si aplica)", "")))
                .findFirst();

        if (conIngrediente.isPresent())
            return extraerNumero(conIngrediente.get().get("Equivalencia aproximada"));

        // Luego buscar sin ingrediente (—)
        Optional<Map<String, String>> sinIngrediente = datos.stream()
                .filter(d -> tiposPermitidos.contains(d.getOrDefault("Tipo lógico", "")))
                .filter(d -> unidad.equalsIgnoreCase(d.get("Unidad base")))
                .filter(d -> {
                    String ingr = d.getOrDefault("Ingrediente (si aplica)", "").trim();
                    return ingr.isEmpty() || ingr.equals("—");
                })
                .findFirst();

        return sinIngrediente.map(d -> extraerNumero(d.get("Equivalencia aproximada"))).orElse(null);
    }

    private static double extraerNumero(String entrada) {
        return Double.parseDouble(entrada.replace(",", ".").replaceAll("[^0-9.]", "").trim());
    }
}
