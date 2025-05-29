package com.panaderiafx.controllers.components.editarproduccion.helpers;

import com.panaderiafx.utils.ConversorMezclaUtils;
import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtilsOptimized;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

public class EscaladoIngredientesUtils {

    private static Map<String, String> mapaNombreIngredientes;

    public static void setMapaNombreIngredientes(Map<String, String> mapa) {
        mapaNombreIngredientes = mapa;
    }

    public static double actualizarIngredientesDesdeCantidad(
            int nuevaCantidad,
            int cantidadBase,
            String codigoReceta,
            ObservableList<Map<String, String>> ingredientesEditable,
            Map<String, String> produccionRef
    ) {
        double rendimiento = ConversorMezclaUtils.obtenerRendimientoReceta(codigoReceta);
        double factor = calcularFactor(nuevaCantidad, cantidadBase, rendimiento, ingredientesEditable);

        if (factor <= 0) return 0;

        double nuevoCostoTotal = ingredientesEditable.stream()
                .peek(fila -> recalcularFilaIngrediente(fila, factor))
                .filter(fila -> "✓".equals(fila.getOrDefault("Check", " ")))
                .mapToDouble(fila -> TotalesProduccionUtils.parseDouble(fila.getOrDefault("Costo", "0")))
                .sum();

        produccionRef.put("Cantidad Base", String.valueOf(nuevaCantidad));
        produccionRef.put("Costo Total", String.format("%.2f", nuevoCostoTotal));
        System.out.println("✅ Ingredientes ajustados correctamente.");

        return ConversorMezclaUtils.calcularMezclasDesdeProduccion(nuevaCantidad, codigoReceta);
    }

    private static double calcularFactor(int nuevaCantidad, int cantidadBase, double rendimiento, ObservableList<Map<String, String>> ingredientesEditable) {
        if (cantidadBase > 0 && !ingredientesEditable.isEmpty()) {
            System.out.printf("🔁 Usando ProduccionIngredientes: factor=%.6f%n", (double) nuevaCantidad / cantidadBase);
            return (double) nuevaCantidad / cantidadBase;
        } else if (rendimiento > 0) {
            System.out.printf("📐 Usando RecetasIngredientes: factor=%.6f%n", (double) nuevaCantidad / rendimiento);
            return (double) nuevaCantidad / rendimiento;
        }
        System.out.println("⚠️ Error: Rendimiento y cantidad base son cero.");
        return 0;
    }

    private static void recalcularFilaIngrediente(Map<String, String> fila, double factor) {
        String codIng = fila.getOrDefault("Ingrediente", "");
        double cantidadBaseIng = TotalesProduccionUtils.parseDouble(fila.getOrDefault("Cantidad Base", "0"));
        String unidad = fila.getOrDefault("Unidades", "");

        if (factor != 1.0) {
            double cantidadFinal = cantidadBaseIng * factor;
            double costoUnitario = calcularCostoUnitario(codIng, unidad);
            double costoTotal = cantidadFinal * costoUnitario;

            fila.put("Cantidad", String.format("%.4f", cantidadFinal));
            fila.put("Costo", String.format("%.4f", costoTotal));
            fila.put("Costo Unitario", String.format("%.4f", costoUnitario));

            System.out.printf("🔄 [%s] %.4f * %.4f = %.4f → $%.4f%n", codIng, cantidadBaseIng, factor, cantidadFinal, costoTotal);
        }

        if (mapaNombreIngredientes != null) {
            fila.put("Nombre Ingrediente", mapaNombreIngredientes.getOrDefault(codIng, ""));
        }
    }

    public static int calcularProduccionDesdeMezcla(double mezcla, String codigoReceta) {
        return ConversorMezclaUtils.calcularProduccionDesdeMezclas(mezcla, codigoReceta);
    }

    public static double obtenerRendimientoReceta(String codigoReceta) {
        return ConversorMezclaUtils.obtenerRendimientoReceta(codigoReceta);
    }

    public static double calcularCostoUnitario(String codIngrediente, String unidadDestino) {
        // ✅ Releer directamente desde archivo con VerUtilsOptimized
        List<Map<String, String>> ingredientes = VerUtilsOptimized.verTabla("Ingredientes");

        return ingredientes.stream()
                .filter(i -> codIngrediente.equalsIgnoreCase(i.get("Código")))
                .findFirst()
                .map(fila -> {
                    String unidadBase = fila.getOrDefault("Unidad", "").trim();
                    double precioBase = TotalesProduccionUtils.parseDouble(fila.getOrDefault("Precio Local", "0"));
                    Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadBase, unidadDestino, 1.0, codIngrediente);
                    return (cantidadConvertida == null || cantidadConvertida == 0) ? 0 : precioBase / cantidadConvertida;
                })
                .orElse(0.0);
    }
}
