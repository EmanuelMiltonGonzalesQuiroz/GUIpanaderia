package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.ConversorUtils;

import java.util.*;

public class CostosDirectosUtils {

    public static double calcular(String fechaSeleccionada, String tipo) {
        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");

        double total = 0.0;
        System.out.println("📄 Filas Producción: " + produccion.size());

        for (Map<String, String> fila : produccion) {
            String fechaFila = fila.getOrDefault("Fecha", "").trim();
            if (!FechaUtils.coincide(fechaFila, fechaSeleccionada, tipo)) continue;

            String codReceta = fila.getOrDefault("Código receta", "").trim();
            double cantidadProducida = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));

            if (codReceta.isEmpty() || cantidadProducida == 0) {
                System.out.println("❌ Receta vacía o cantidad 0, omitiendo fila");
                continue;
            }

            System.out.printf("🧾 Receta producida: %s | Cantidad: %.2f\n", codReceta, cantidadProducida);

            OptionalDouble rendimientoOpt = recetas.stream()
                .filter(r -> codReceta.equalsIgnoreCase(r.getOrDefault("Código Receta", "").trim()))
                .mapToDouble(r -> ParseUtils.toDouble(r.getOrDefault("Rendimiento", "0")))
                .filter(r -> r > 0)
                .findFirst();

            if (rendimientoOpt.isEmpty()) {
                System.out.printf("❌ No se encontró rendimiento válido para receta %s, omitiendo cálculo.\n", codReceta);
                continue;
            }

            double rendimiento = rendimientoOpt.getAsDouble();
            double factorProduccion = cantidadProducida / rendimiento;
            System.out.printf("   🔧 Rendimiento: %.2f | Factor producción: %.4f\n", rendimiento, factorProduccion);

            List<Map<String, String>> ingredientesReceta = recetasIngredientes.stream()
                .filter(f -> codReceta.equalsIgnoreCase(f.getOrDefault("Código Receta", "").trim()))
                .toList();

            System.out.printf("   🧂 Ingredientes asociados: %d\n", ingredientesReceta.size());

            for (Map<String, String> ing : ingredientesReceta) {
                String codIng = ing.getOrDefault("Ingrediente", "").trim();
                double cantidadIngReceta = ParseUtils.toDouble(ing.getOrDefault("Cantidad", "0"));
                String unidadReceta = ing.getOrDefault("Unidades", "").trim();

                if (codIng.isEmpty() || cantidadIngReceta == 0) {
                    System.out.println("⚠️ Ingrediente vacío o cantidad 0, omitiendo");
                    continue;
                }

                Map<String, String> filaIng = ingredientes.stream()
                    .filter(f -> codIng.equalsIgnoreCase(f.getOrDefault("Código", "").trim()))
                    .findFirst().orElse(null);

                if (filaIng == null) {
                    System.out.printf("⚠️ Ingrediente %s no encontrado en hoja Ingredientes\n", codIng);
                    continue;
                }

                double precio = ParseUtils.toDouble(filaIng.getOrDefault("Precio Local", "0"));
                String unidadIng = filaIng.getOrDefault("Unidad", "").trim();

                Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadReceta, unidadIng, cantidadIngReceta, codIng);
                if (cantidadConvertida == null || cantidadConvertida <= 0) {
                    System.out.printf("⚠️ Fallo conversión %s → %s | Cantidad: %.2f\n", unidadReceta, unidadIng, cantidadIngReceta);
                    continue;
                }

                double costo = cantidadConvertida * factorProduccion * precio;

                System.out.printf("     ➤ Ingrediente: %s | %.2f %s → %.2f %s | Precio: %.2f | Costo: %.2f\n",
                        codIng, cantidadIngReceta, unidadReceta, cantidadConvertida, unidadIng, precio, costo);

                total += costo;
            }
        }

        System.out.printf("✅ Total costos directos: %.2f\n", total);
        return total;
    }
}
