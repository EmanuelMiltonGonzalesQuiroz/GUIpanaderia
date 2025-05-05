package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;

import java.util.*;

public class CostosIndirectosUtils {

    public static double calcular(String tipo) {
        List<Map<String, String>> costos = VerUtils.verTabla("Costos");

        System.out.println("   📄 Filas Costos: " + costos.size());

        double total = 0;

        for (Map<String, String> fila : costos) {
            String tipoCosto = fila.getOrDefault("Tipo", "").trim();
            if (!tipoCosto.equalsIgnoreCase("Indirecto")) continue;

            double precio = ParseUtils.toDouble(fila.getOrDefault("Precio Local", "0"));
            if (precio <= 0) continue;

            String frecuencia = fila.getOrDefault("Frecuencia", "").trim().toLowerCase();

            double ajustado = switch (frecuencia) {
                case "mensual" -> tipo.equalsIgnoreCase("DÍA") ? precio / 30 : precio;
                case "semanal" -> tipo.equalsIgnoreCase("DÍA") ? precio / 7 : precio * 4;
                default -> 0;
            };

            System.out.printf("     ➤ Costo indirecto: %.2f (ajustado: %.2f) | Frecuencia: %s\n",
                              precio, ajustado, frecuencia);

            total += ajustado;
        }

        System.out.printf("   Total costos indirectos: %.2f\n", total);
        return total;
    }
}
