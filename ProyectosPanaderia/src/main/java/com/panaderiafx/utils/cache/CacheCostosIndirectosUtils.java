package com.panaderiafx.utils.cache;

import java.util.*;

public class CacheCostosIndirectosUtils {
    private static double valor = 0;
    private static final List<Runnable> observadores = new ArrayList<>();

    public static void set(double nuevoValor) {
        valor = nuevoValor;
        System.out.printf("💾 Guardando Costos Indirectos: %.2f\n", valor);
        notificar();
    }

    public static double get() {
        return valor;
    }

    public static void limpiar() {
        valor = 0;
        System.out.println("🧹 Caché de Costos Indirectos limpiado.");
        notificar();
    }

    public static void agregarObservador(Runnable obs) {
        observadores.add(obs);
    }

    private static void notificar() {
        for (Runnable o : observadores) o.run();
    }
    
}
