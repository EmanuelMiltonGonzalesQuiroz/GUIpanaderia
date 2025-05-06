package com.panaderiafx.utils.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheCostosDirectosUtils {

    private static final Map<String, Double> cache = new HashMap<>();

    public static String clave(String codReceta, double cantidad) {
        return codReceta.trim() + "|" + cantidad;
    }

    public static boolean contiene(String codReceta, double cantidad) {
        return cache.containsKey(clave(codReceta, cantidad));
    }

    public static Double obtener(String codReceta, double cantidad) {
        return cache.get(clave(codReceta, cantidad));
    }

    public static void guardar(String codReceta, double cantidad, double costo) {
        cache.put(clave(codReceta, cantidad), costo);
    }

    public static void limpiar() {
        cache.clear();
    }

}
