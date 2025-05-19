package com.panaderiafx.utils;

import com.panaderiafx.utils.cache.*;
import com.panaderiafx.utils.componentes.*;

import java.util.*;

public class VistaResumenUtils {

    public static Map<String, Double> calcularResumen(String fecha, String tipo) {
        System.out.println("🔍 Calculando resumen para:");
        System.out.println("   ➤ Fecha seleccionada: " + fecha);
        System.out.println("   ➤ Tipo seleccionado: " + tipo);

        Map<String, Double> resultado = new HashMap<>();

        double ganancias = GananciasUtils.calcular(fecha, tipo);
        CacheGananciasUtils.set(ganancias); // 👈 esto debe ir antes del cálculo de parámetros

        double costosDirectos = CostosDirectosUtils.calcular(fecha, tipo);
        double costosIndirectos = CostosIndirectosUtils.calcular(tipo);
        double parametros = ParametrosUtils.calcular(tipo); // ahora sí, ya con ganancia actualizada

        double total = ganancias - costosDirectos - costosIndirectos - parametros;

        // GUARDADO EN CACHÉ + LOG
        System.out.printf("💾 Guardando en caché: Ganancias = %.2f\n", ganancias);
        CacheGananciasUtils.set(ganancias);

        System.out.printf("💾 Guardando en caché: Costos Directos = %.2f\n", costosDirectos);
        CacheCostosDirectosUtils.set(costosDirectos);

        resultado.put("ganancias", ganancias);
        resultado.put("costos_directos", costosDirectos);
        resultado.put("total", total);

        System.out.println("📊 RESUMEN FINAL:");
        System.out.printf("   ➤ Ganancias: %.2f\n", ganancias);
        System.out.printf("   ➤ Costos Directos: %.2f\n", costosDirectos);
        System.out.printf("   ➤ TOTAL: %.2f\n", total);

        return resultado;
    }
}
