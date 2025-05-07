package com.panaderiafx.utils.cache;

import java.util.*;

public class CacheCostosDirectosUtils {

    private static final Map<String, Double> cache = new HashMap<>();
    private static final List<Runnable> observadores = new ArrayList<>();

    private static double total = 0;

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
        String key = clave(codReceta, cantidad);
        boolean existe = cache.containsKey(key);
        cache.put(key, costo);
        recalcularTotal();
        System.out.printf("💾 %s costo directo: [%s] = %.2f\n", existe ? "Editando" : "Agregando", key, costo);
        System.out.printf("   ➤ Total costos directos actualizado: %.2f\n", total);
        notificarObservadores();
    }

    public static void editar(String codReceta, double cantidad, double nuevoCosto) {
        guardar(codReceta, cantidad, nuevoCosto);
    }

    public static void limpiar() {
        cache.clear();
        total = 0;
        System.out.println("🧹 Caché de costos directos limpiado.");
        notificarObservadores();
    }

    public static void agregarObservador(Runnable obs) {
        observadores.add(obs);
    }

    private static void notificarObservadores() {
        for (Runnable r : observadores) r.run();
    }

    public static double total() {
        return total;
    }

    public static void setTotal(double nuevoTotal) {
        total = nuevoTotal;
        System.out.printf("📝 Total costos directos forzado a: %.2f\n", total);
        notificarObservadores();
    }

    private static void recalcularTotal() {
        total = cache.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    private static final Map<String, Double> cacheUnidad = new HashMap<>();

    public static void guardarUnidad(String codReceta, double costoUnidad) {
        cacheUnidad.put(codReceta, costoUnidad);
    }

    public static boolean contieneUnidad(String codReceta) {
        return cacheUnidad.containsKey(codReceta);
    }

    public static double obtenerUnidad(String codReceta) {
        return cacheUnidad.getOrDefault(codReceta, 0.0);
    }

}
