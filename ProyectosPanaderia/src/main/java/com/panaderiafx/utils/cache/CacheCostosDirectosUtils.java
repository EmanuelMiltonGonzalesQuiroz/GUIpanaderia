package com.panaderiafx.utils.cache;

import java.util.*;

public class CacheCostosDirectosUtils {

    private static final Map<String, Double> cache = new HashMap<>();
    private static final Map<String, Double> cacheUnidad = new HashMap<>();
    private static final List<Runnable> observadores = new ArrayList<>();

    private static double total = 0;

    public static boolean contiene(String codProduccion) {
        return cache.containsKey(codProduccion);
    }

    public static Double obtener(String codProduccion) {
        return cache.get(codProduccion);
    }

    public static void guardar(String codProduccion, double costo) {
        cache.put(codProduccion, costo);
        recalcularTotal();
        System.out.printf("💾 Guardando costo directo: [%s] = %.2f\n", codProduccion, costo);
        System.out.printf("   ➤ Total costos directos actualizado: %.2f\n", total);
        notificarObservadores();
    }

    public static void editar(String codProduccion, double nuevoCosto) {
        guardar(codProduccion, nuevoCosto);
    }

    public static void limpiar() {
        cache.clear();
        cacheUnidad.clear();
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

    private static void recalcularTotal() {
        total = cache.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public static double get() {
        return total;
    }

    public static void set(double nuevoTotal) {
        total = nuevoTotal;
        System.out.printf("📝 Total costos directos forzado a: %.2f\n", total);
        notificarObservadores();
    }

    public static void guardarUnidad(String codProduccion, double costoUnidad) {
        cacheUnidad.put(codProduccion, costoUnidad);
    }

    public static boolean contieneUnidad(String codProduccion) {
        return cacheUnidad.containsKey(codProduccion);
    }

    public static double obtenerUnidad(String codProduccion) {
        return cacheUnidad.getOrDefault(codProduccion, 0.0);
    }
}
