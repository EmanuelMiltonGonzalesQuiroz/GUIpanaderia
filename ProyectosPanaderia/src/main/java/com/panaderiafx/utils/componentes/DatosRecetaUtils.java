package com.panaderiafx.utils.componentes;

import java.util.Map;

public class DatosRecetaUtils {

    public static double calcularFactorProduccion(Map<String, String> filaReceta, double cantidadProducida) {
        double rendimiento = ParseUtils.toDouble(filaReceta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) return 0;
        return cantidadProducida / rendimiento;
    }

    public static void logInicioReceta(String codReceta, double cantidad, double rendimiento, double factor, int ingredientes) {
        System.out.printf("📦 Receta %s | Cantidad: %.2f | Rendimiento: %.2f | Factor: %.4f | Ingredientes: %d\n",
                codReceta, cantidad, rendimiento, factor, ingredientes);
    }

    public static void logTotalReceta(String codReceta, double total, int count, double tiempo) {
        System.out.printf("✅ Total receta %s: %.2f | Ingredientes procesados: %d | Tiempo total: %.2f seg\n",
                codReceta, total, count, tiempo);
    }
}
