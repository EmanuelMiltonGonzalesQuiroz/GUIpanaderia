package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;

import java.util.*;

public class GananciasUtils {

    public static double calcular(String fecha, String tipo) {
        double total = 0;
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        System.out.println("📄 Filas Producción: " + produccion.size());

        for (Map<String, String> fila : produccion) {
            String fechaFila = fila.getOrDefault("Fecha", "").trim();
            if (!FechaUtils.coincide(fechaFila, fecha, tipo)) continue;

            double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            double precioUnidad = ParseUtils.toDouble(fila.getOrDefault("Precio de Venta por Unidad", "0"));
            double ganancia = cantidad * precioUnidad;

            total += ganancia;
        }

        System.out.printf("✅ Total ganancias: %.2f\n", total);
        return total;
    }
}
