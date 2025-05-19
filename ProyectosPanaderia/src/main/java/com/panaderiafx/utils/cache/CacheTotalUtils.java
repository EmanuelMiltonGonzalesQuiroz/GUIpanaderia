package com.panaderiafx.utils.cache;

import java.util.*;

public class CacheTotalUtils {
    private static double total = 0;
    private static final List<Runnable> observadores = new ArrayList<>();

    public static void recalcular() {
        total = CacheGananciasUtils.get()
              - CacheCostosDirectosUtils.get();
        System.out.printf("📊 Recalculando TOTAL: %.2f\n", total);
        notificar();
    }

    public static double get() {
        return total;
    }

    public static void limpiar() {
        total = 0;
        System.out.println("🧹 Caché de TOTAL limpiado.");
        notificar();
    }

    public static void agregarObservador(Runnable obs) {
        observadores.add(obs);
    }

    private static void notificar() {
        for (Runnable o : observadores) o.run();
    }
}
