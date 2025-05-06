package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.*;
public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        long tiempoInicio = System.nanoTime();
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");

        // Si ya hay caché previa, limpiarla para este ciclo
        CacheCostosDirectosUtils.limpiar();

        System.out.println("📄 Filas Producción: " + produccion.size());

        double total = 0;
        int recetasProcesadas = 0;

        for (Map<String, String> fila : produccion) {
            String fechaFila = fila.getOrDefault("Fecha", "").trim();
            if (!FechaUtils.coincide(fechaFila, fechaSeleccionada, tipo)) continue;

            String codReceta = fila.getOrDefault("Código receta", "").trim();
            double cantidadProducida = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            if (codReceta.isEmpty() || cantidadProducida <= 0) continue;

            // Consulta por caché primero
            double totalReceta;
            if (CacheCostosDirectosUtils.contiene(codReceta, cantidadProducida)) {
                totalReceta = CacheCostosDirectosUtils.obtener(codReceta, cantidadProducida);
                System.out.printf("♻️ Recuperado de caché: %s x %.2f = %.2f\n", codReceta, cantidadProducida, totalReceta);
            } else {
                long t0 = System.nanoTime();
                totalReceta = CostosDirectosPorRecetaUtils.calcular(codReceta, cantidadProducida);
                long t1 = System.nanoTime();
                CacheCostosDirectosUtils.guardar(codReceta, cantidadProducida, totalReceta);
                System.out.printf("🧮 Calculado: %s x %.2f = %.2f (%.2f seg)\n",
                        codReceta, cantidadProducida, totalReceta, (t1 - t0) / 1e9);
            }

            total += totalReceta;
            recetasProcesadas++;
        }

        long tiempoFin = System.nanoTime();
        double duracion = (tiempoFin - tiempoInicio) / 1e9;

        System.out.printf("✅ Total general: %.2f | Recetas procesadas: %d | Tiempo total: %.2f seg\n", total, recetasProcesadas, duracion);
        return total;
    }
}
