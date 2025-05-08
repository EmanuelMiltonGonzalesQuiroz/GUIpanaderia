package com.panaderiafx.utils.cache;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParametrosUtils;

import java.util.*;

public class CacheParametrosUtils {
    private static double valor = 0;
    private static final List<Runnable> observadores = new ArrayList<>();

    public static void set(double nuevoValor) {
        valor = nuevoValor;
        System.out.printf("💾 Guardando Parámetros: %.2f\n", valor);
        notificar();
    }

    public static double get() {
        return valor;
    }

    public static void limpiar() {
        valor = 0;
        System.out.println("🧹 Caché de Parámetros limpiado.");
        notificar();
    }

    public static void agregarObservador(Runnable obs) {
        observadores.add(obs);
    }

    private static void notificar() {
        for (Runnable o : observadores) o.run();
    }

    public static void editar(String codigo, double nuevoValor) {
        List<Map<String, String>> parametros = VerUtils.verTabla("Parametros");

        for (Map<String, String> fila : parametros) {
            if (fila.getOrDefault("Código", "").equalsIgnoreCase(codigo)) {
                fila.put("Valor", String.format("%.2f", nuevoValor));
                break;
            }
        }

        recalcular(); // actualiza con la tabla completa tras editar
    }

    public static void recalcular() {
        double ganancias = CacheGananciasUtils.get();
        System.out.printf("🔁 Usando ganancia actual: %.2f para parámetros\n", ganancias);

        double nuevoTotal = ParametrosUtils.calcular("DÍA");
        set(nuevoTotal);
    }

    public static void recalcular(List<Map<String, String>> parametros) {
        double ganancias = CacheGananciasUtils.get();
        System.out.printf("🔁 Usando ganancia actual (desde lista): %.2f para parámetros\n", ganancias);

        double nuevoTotal = ParametrosUtils.calcular("DÍA");
        set(nuevoTotal);
    }
    
}
