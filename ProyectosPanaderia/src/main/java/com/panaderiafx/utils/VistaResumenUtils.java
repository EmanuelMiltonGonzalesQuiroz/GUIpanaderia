package com.panaderiafx.utils;

import com.panaderiafx.utils.componentes.*;

import java.util.*;

public class VistaResumenUtils {

    public static Map<String, Double> calcularResumen(String fecha, String tipo) {
        System.out.println("🔍 Calculando resumen para:");
        System.out.println("   ➤ Fecha seleccionada: " + fecha);
        System.out.println("   ➤ Tipo seleccionado: " + tipo);

        Map<String, Double> resultado = new HashMap<>();

        double ganancias = GananciasUtils.calcular(fecha, tipo);
        double costosDirectos = CostosDirectosUtils.calcular(fecha, tipo);
        double costosIndirectos = CostosIndirectosUtils.calcular(tipo);
        double parametros = ParametrosUtils.calcular(tipo);

        double total = ganancias - costosDirectos - costosIndirectos - parametros;

        resultado.put("ganancias", ganancias);
        resultado.put("costos_directos", costosDirectos);
        resultado.put("costos_indirectos", costosIndirectos);
        resultado.put("parametros", parametros);
        resultado.put("total", total);

        System.out.println("📊 RESUMEN:");
        System.out.printf("   Ganancias: %.2f\n", ganancias);
        System.out.printf("   Costos Directos: %.2f\n", costosDirectos);
        System.out.printf("   Costos Indirectos: %.2f\n", costosIndirectos);
        System.out.printf("   Descuentos/Impuestos: %.2f\n", parametros);
        System.out.printf("   TOTAL: %.2f\n", total);

        return resultado;
    }
}
