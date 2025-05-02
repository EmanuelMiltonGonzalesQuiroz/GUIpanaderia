package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import java.util.*;

public class CalculadorProduccion {

    public static List<Map<String, String>> generarResumen(String modo) {
        List<Map<String, String>> produccion = VerUtils.verTabla("Producción");
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> costos = VerUtils.verTabla("ParámetrosGenerales");

        double ajusteMensual = "Mensual".equalsIgnoreCase(modo) ? 1.0 : 1.0 / 30;
        double ajusteSemanal = "Mensual".equalsIgnoreCase(modo) ? 4.0 : 1.0 / 7;

        List<Map<String, String>> resultado = new ArrayList<>();

        for (Map<String, String> fila : produccion) {
            String codigo = fila.get("Código Receta");
            String cantidadStr = fila.get("Cantidad");
            int cantidad = Integer.parseInt(cantidadStr);
            Map<String, String> receta = recetas.stream()
                .filter(r -> codigo.equals(r.get("Código")))
                .findFirst()
                .orElse(Map.of());

            String producto = receta.getOrDefault("Nombre", "Sin nombre");
            double ganancia = parseDoubleSafe(receta.get("Ganancia Esperada")) * cantidad;
            double costoIng = parseDoubleSafe(receta.get("Costo Total")) * cantidad;

            double costosDirectos = obtenerCosto("Directo", costos) * ajusteMensual;
            double costosIndirectos = obtenerCosto("Indirecto", costos) * ajusteSemanal;

            Map<String, String> resumen = new LinkedHashMap<>();
            resumen.put("Producto", producto);
            resumen.put("Cantidad", String.valueOf(cantidad));
            resumen.put("Ganancia Estimada", String.format("%.2f", ganancia));
            resumen.put("Gasto en Ingredientes", String.format("%.2f", costoIng));
            resumen.put("Costos Directos", String.format("%.2f", costosDirectos));
            resumen.put("Costos Indirectos", String.format("%.2f", costosIndirectos));

            resultado.add(resumen);
        }

        return resultado;
    }

    private static double parseDoubleSafe(String texto) {
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }

    private static double obtenerCosto(String tipo, List<Map<String, String>> parametros) {
        return parametros.stream()
            .filter(p -> tipo.equalsIgnoreCase(p.getOrDefault("Tipo", "")))
            .map(p -> parseDoubleSafe(p.get("Valor")))
            .reduce(0.0, Double::sum);
    }
}
