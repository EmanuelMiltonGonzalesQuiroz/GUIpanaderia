package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.ConversorUtils;

import java.util.Map;

public class CalculoCostosUtils {

    public static double calcularCostoIngrediente(
            Map<String, String> ingredienteReceta,
            Map<String, String> filaIngrediente,
            double factorProduccion,
            String codIngrediente
    ) {
        double cantidad = ParseUtils.toDouble(ingredienteReceta.getOrDefault("Cantidad", "0"));
        String unidadReceta = ingredienteReceta.getOrDefault("Unidades", "").trim();
        String unidadIngrediente = filaIngrediente.getOrDefault("Unidad", "").trim();
        double precio = ParseUtils.toDouble(filaIngrediente.getOrDefault("Precio Local", "0"));

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadReceta, unidadIngrediente, cantidad, codIngrediente);

        if (cantidadConvertida == null || cantidadConvertida <= 0) {
            System.out.printf("⚠️ Fallo conversión %s → %s | %s\n", unidadReceta, unidadIngrediente, codIngrediente);
            return 0;
        }

        double costo = cantidadConvertida * factorProduccion * precio;
        System.out.printf("   ➤ Ingrediente %s | %.2f %s → %.2f %s | Precio: %.2f | Costo: %.2f\n",
                codIngrediente, cantidad, unidadReceta, cantidadConvertida, unidadIngrediente, precio, costo);
        return costo;
    }
}
