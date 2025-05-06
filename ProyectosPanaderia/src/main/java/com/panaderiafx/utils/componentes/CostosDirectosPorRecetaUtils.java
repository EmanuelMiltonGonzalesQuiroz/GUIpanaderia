package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CostosDirectosPorRecetaUtils {

    public static double calcular(String codReceta, double cantidadProducida) {
        long t0 = System.currentTimeMillis();
        System.out.printf("🔍 Iniciando cálculo de costos directos para receta %s | cantidad: %.2f\n", codReceta, cantidadProducida);

        if (codReceta == null || codReceta.isBlank() || cantidadProducida <= 0) return 0;

        // Verificamos si ya está en caché
        if (CacheCostosDirectosUtils.contiene(codReceta, cantidadProducida)) {
            double valor = CacheCostosDirectosUtils.obtener(codReceta, cantidadProducida);
            System.out.printf("✅ Resultado recuperado de caché: %.2f\n", valor);
            return valor;
        }

        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        long t1 = System.currentTimeMillis();
        System.out.printf("📥 Datos cargados (%.2f seg)\n", (t1 - t0) / 1000.0);

        Map<String, Map<String, String>> mapaIngredientes = ingredientes.stream()
                .collect(Collectors.toMap(f -> f.getOrDefault("Código", "").trim(), f -> f, (a, b) -> a));

        Map<String, Map<String, String>> mapaRecetas = recetas.stream()
                .collect(Collectors.toMap(f -> f.getOrDefault("Código receta", "").trim(), f -> f, (a, b) -> a));

        Map<String, List<Map<String, String>>> mapaRecetasIngredientes = recetasIngredientes.stream()
                .collect(Collectors.groupingBy(f -> f.getOrDefault("Código receta", "").trim()));

        Map<String, String> filaReceta = mapaRecetas.getOrDefault(codReceta, null);
        if (filaReceta == null) {
            System.out.println("❌ Receta no encontrada.");
            return 0;
        }

        double rendimiento = ParseUtils.toDouble(filaReceta.getOrDefault("Rendimiento", "0"));
        if (rendimiento <= 0) {
            System.out.println("⚠️ Rendimiento inválido.");
            return 0;
        }

        double factorProduccion = cantidadProducida / rendimiento;
        double total = 0;

        System.out.printf("🧾 Rendimiento: %.2f | Factor producción: %.4f\n", rendimiento, factorProduccion);

        List<Map<String, String>> listaIngredientes = mapaRecetasIngredientes.getOrDefault(codReceta, List.of());

        int contador = 0;
        for (Map<String, String> ing : listaIngredientes) {
            contador++;
            String codIng = ing.getOrDefault("Ingrediente", "").trim();
            double cantidadIngReceta = ParseUtils.toDouble(ing.getOrDefault("Cantidad", "0"));
            String unidadReceta = ing.getOrDefault("Unidades", "").trim();

            Map<String, String> filaIng = mapaIngredientes.getOrDefault(codIng, null);
            if (filaIng == null || cantidadIngReceta <= 0) continue;

            double precio = ParseUtils.toDouble(filaIng.getOrDefault("Precio Local", "0"));
            String unidadIng = filaIng.getOrDefault("Unidad", "").trim();

            Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadReceta, unidadIng, cantidadIngReceta, codIng);
            if (cantidadConvertida == null || cantidadConvertida <= 0) {
                System.out.printf("⚠️ Fallo conversión %s → %s para %s\n", unidadReceta, unidadIng, codIng);
                continue;
            }

            double costo = cantidadConvertida * factorProduccion * precio;
            System.out.printf("   ➤ Ingrediente %d: %s | %.2f %s → %.2f %s | Precio: %.2f | Costo: %.2f\n",
                    contador, codIng, cantidadIngReceta, unidadReceta, cantidadConvertida, unidadIng, precio, costo);

            total += costo;
        }

        CacheCostosDirectosUtils.guardar(codReceta, cantidadProducida, total);

        long tf = System.currentTimeMillis();
        System.out.printf("✅ Total receta %s: %.2f | Ingredientes procesados: %d | Tiempo total: %.2f seg\n",
                codReceta, total, contador, (tf - t0) / 1000.0);

        return total;
    }
}
