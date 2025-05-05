package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;

import java.util.*;

public class ParametrosUtils {

    public static double calcular(String tipo) {
        List<Map<String, String>> parametros = VerUtils.verTabla("Parametros");
        System.out.println("   📄 Filas Parámetros: " + parametros.size());

        double total = 0.0;

        double costoManoObra = 0.0;
        double porcentajeMerma = 0.0;
        double porcentajeImpuesto = 0.0;
        int empleados = 1;

        for (Map<String, String> param : parametros) {
            String codigo = param.getOrDefault("Código", "");
            double valor = ParseUtils.toDouble(param.getOrDefault("Valor", "0"));
            String unidad = param.getOrDefault("Unidad", "").toLowerCase();

            switch (codigo) {
                case "PAR0001" -> { // Costo Mano Obra Diario
                    costoManoObra = valor;
                    System.out.printf("     ➤ Mano de obra diaria detectada: %.2f %s\n", valor, unidad);
                }
                case "PAR0002" -> {
                    porcentajeMerma = valor;
                    System.out.printf("     ➤ Merma estimada: %.2f%%\n", valor);
                }
                case "PAR0006" -> {
                    porcentajeImpuesto = valor;
                    System.out.printf("     ➤ Impuesto aplicado: %.2f%%\n", valor);
                }
                case "PAR0007" -> {
                    empleados = (int) valor;
                    System.out.printf("     ➤ Empleados activos considerados: %d\n", empleados);
                }
            }
        }

        // Calcular componentes individuales
        double manoObraTotal = costoManoObra * empleados;
        double impuestoTotal = manoObraTotal * (porcentajeImpuesto / 100);
        double mermaCosto = manoObraTotal * (porcentajeMerma / 100);

        System.out.printf("     ➤ Costo total mano de obra: %.2f\n", manoObraTotal);
        System.out.printf("     ➤ Carga fiscal: %.2f\n", impuestoTotal);
        System.out.printf("     ➤ Costo por merma: %.2f\n", mermaCosto);

        total = manoObraTotal + impuestoTotal + mermaCosto;
        System.out.printf("   Total descuentos/impuestos: %.2f\n", total);

        return total;
    }
}
