package com.panaderiafx.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CacheLibroSemanal {

    public enum Tipo {
        GANANCIA_B,
        COSTO_DIRECTO,
        COSTO_INDIRECTO,
        PARAMETROS,
        COSTOS_DIA,
        TOTAL
    }

    private static final Map<Tipo, Double> valores = new HashMap<>();
    private static final Map<Tipo, Consumer<Double>> observadores = new HashMap<>();

    static {
        for (Tipo tipo : Tipo.values()) {
            valores.put(tipo, 0.0);
        }
    }

    public static void set(Tipo tipo, double valor) {
        valores.put(tipo, valor);
        System.out.println("📦 Cache actualizado: " + tipo + " = " + valor);
        if (observadores.containsKey(tipo)) {
            observadores.get(tipo).accept(valor);
        }
        if (tipo != Tipo.TOTAL) {
            recalcularTotal();
        }
    }

    public static double get(Tipo tipo) {
        return valores.getOrDefault(tipo, 0.0);
    }

    public static void reiniciar() {
        for (Tipo tipo : Tipo.values()) {
            set(tipo, 0.0);
        }
    }

    public static void onChange(Tipo tipo, Consumer<Double> listener) {
        observadores.put(tipo, listener);
        listener.accept(get(tipo)); // 🔥 Disparo inmediato
    }

    private static void recalcularTotal() {
        double total = get(Tipo.GANANCIA_B)
                     - get(Tipo.COSTO_DIRECTO)
                     - get(Tipo.COSTO_INDIRECTO)
                     - get(Tipo.PARAMETROS)
                     + get(Tipo.COSTOS_DIA);
        set(Tipo.TOTAL, total); // reutiliza set para notificar observador
    }
}
