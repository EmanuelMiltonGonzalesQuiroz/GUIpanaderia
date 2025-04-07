package com.panaderiafx.utils;

import java.util.*;

public class IdentificadorUtils {

    public static String construirIdentificador(String tabla, Map<String, String> registro) {
        Map<String, String> normalizado = new HashMap<>();
        for (Map.Entry<String, String> entry : registro.entrySet()) {
            normalizado.put(entry.getKey().toLowerCase().trim(), entry.getValue());
        }
    
        switch (tabla) {
            case "Ingredientes":
                return normalizado.getOrDefault("nombre", "").trim();
            case "Recetas":
                return normalizado.getOrDefault("producto", "").trim() + " - " + normalizado.getOrDefault("ingrediente", "").trim();
            case "Produccion":
                return normalizado.getOrDefault("fecha", "").trim() + " - " + normalizado.getOrDefault("codigo receta", "").trim() + " - " + normalizado.getOrDefault("lote", "").trim();
            case "Costos":
                return normalizado.getOrDefault("nombre", "").trim();
            case "TasaCambio":
                return normalizado.getOrDefault("fecha", "").trim();
            case "HistorialPrecios":
                return normalizado.getOrDefault("codigo", "").trim() + " - " + normalizado.getOrDefault("fecha", "").trim();
            case "Parametros":
                return normalizado.getOrDefault("nombre", "").trim();
            case "TabladeConversión":
                return normalizado.getOrDefault("unidad base", "").trim();
            default:
                return normalizado.toString();
        }
    }
    public static List<String> obtenerSecundarios(String tabla, List<Map<String, String>> registros, String principal) {
        return registros.stream()
            .filter(r -> construirIdentificador(tabla, r).equals(principal))
            .map(r -> {
                switch (tabla) {
                    case "Ingredientes": return r.getOrDefault("unidad", "").trim();
                    case "Recetas": return r.getOrDefault("ingrediente", "").trim();
                    default: return r.getOrDefault("codigo", "").trim(); // fallback
                }
            })
            .distinct()
            .toList();
    }
    
    public static Optional<Map<String, String>> obtenerRegistro(String tabla, List<Map<String, String>> registros, String principal, String secundario) {
        return registros.stream()
            .filter(r -> construirIdentificador(tabla, r).equals(principal))
            .filter(r -> {
                switch (tabla) {
                    case "Ingredientes": return r.getOrDefault("unidad", "").trim().equals(secundario);
                    case "Recetas": return r.getOrDefault("ingrediente", "").trim().equals(secundario);
                    default: return true;
                }
            }).findFirst();
    }
    
    
    public static boolean coincideConSeleccion(String tabla, Map<String, String> registro, String seleccionado) {
        return construirIdentificador(tabla, registro).equals(seleccionado);
    }
    public static String getIdentificadorPrimario(String tabla, Map<String, String> registro) {
        Map<String, String> normalizado = new HashMap<>();
        for (Map.Entry<String, String> entry : registro.entrySet()) {
            normalizado.put(entry.getKey().toLowerCase().trim(), entry.getValue());
        }
    
        return switch (tabla) {
            case "Ingredientes" -> normalizado.getOrDefault("nombre", "").trim();
            case "Recetas" -> normalizado.getOrDefault("producto", "").trim();
            case "Produccion" -> normalizado.getOrDefault("fecha", "").trim();
            case "Costos" -> normalizado.getOrDefault("nombre", "").trim();
            case "TasaCambio" -> normalizado.getOrDefault("fecha", "").trim();
            case "HistorialPrecios" -> normalizado.getOrDefault("codigo", "").trim();
            case "Parametros" -> normalizado.getOrDefault("nombre", "").trim();
            case "TabladeConversión" -> normalizado.getOrDefault("unidad base", "").trim();
            default -> "";
        };
    }
    
    
    public static String getIdentificadorSecundario(String tabla, Map<String, String> registro) {
        return switch (tabla) {
            case "Ingredientes" -> registro.getOrDefault("unidad", "").trim();
            case "Recetas" -> registro.getOrDefault("ingrediente", "").trim();
            case "Produccion" -> registro.getOrDefault("codigo receta", "").trim() + " - " + registro.getOrDefault("lote", "").trim();
            case "HistorialPrecios" -> registro.getOrDefault("fecha", "").trim();
            case "TabladeConversión" -> registro.getOrDefault("unidad destino", "").trim();
            default -> "-";
        };
    }
    
}
