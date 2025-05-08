package com.panaderiafx.utils.cache;

import com.panaderiafx.utils.componentes.ParseUtils;

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

    public static void recalcular(List<Map<String, String>> datosVisuales) {
        double total = 0;
    
        for (Map<String, String> fila : datosVisuales) {
            if (!fila.getOrDefault("Tipo", "").equalsIgnoreCase("Indirecto")) continue;
            if (!fila.getOrDefault("Check", "✓").equals("✓")) continue;
    
            double precio = ParseUtils.toDouble(fila.getOrDefault("Precio Local", "0"));
            String frecuencia = fila.getOrDefault("Frecuencia", "").toLowerCase();
    
            double ajustado = switch (frecuencia) {
                case "mensual" -> precio / 30;
                case "semanal" -> precio / 7;
                default -> 0;
            };
    
            System.out.printf("🔧 Costo Indirecto [%s] %.2f (%s) → %.2f ajustado\n",
                    fila.getOrDefault("Item", "?"), precio, frecuencia, ajustado);
    
            total += ajustado;
        }
    
        set(total);
    }
    
}
