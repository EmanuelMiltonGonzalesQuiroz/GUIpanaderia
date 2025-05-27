package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.controllers.components.editarproduccion.receta.PanelIngredientesEstiloUtils;
import com.panaderiafx.controllers.components.editarproduccion.receta.PanelIngredientesTablaFactory;
import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.BiConsumer;

public class PanelIngredientesEditorFactory {

    public static VBox crear(Map<String, String> produccion,
                             ObservableList<Map<String, String>> datos,
                             BiConsumer<String, Double> actualizarCostoEnTabla,
                             Map<String, String> mapaNombreIngredientes) {

        String codigoProduccion = produccion.get("Código Producción");
        String codigoReceta = produccion.get("Código Receta");
        int cantidadActual = parseInt(produccion.getOrDefault("Cantidad Producida", "0"));
        datos.clear();

        List<Map<String, String>> filaProduccion = VerUtils.verFilas("Produccion", Map.of("Código Producción", codigoProduccion));
        int cantidadBase = filaProduccion.isEmpty() ? 0 : parseInt(filaProduccion.get(0).getOrDefault("Cantidad Producida", "0"));

        List<Map<String, String>> ingredientesProduccion = VerUtils.verFilas("ProduccionIngredientes", Map.of("Código Producción", codigoProduccion));

        if (!ingredientesProduccion.isEmpty()) {
            double factor = (cantidadBase > 0) ? (double) cantidadActual / cantidadBase : 1.0;

            if (cantidadActual != cantidadBase) {
                System.out.printf("🔁 Cantidad producida cambiada: base=%d → nueva=%d, factor=%.4f%n", cantidadBase, cantidadActual, factor);
            }

            for (Map<String, String> fila : ingredientesProduccion) {
                double originalCantidad = parseDouble(fila.getOrDefault("Cantidad Usada", "0"));
                double originalCosto = parseDouble(fila.getOrDefault("Costo Total", "0"));
                double nuevaCantidad = originalCantidad * factor;
                double nuevoCosto = originalCosto * factor;
                double costoUnitario = originalCantidad == 0 ? 0 : originalCosto / originalCantidad;

                Map<String, String> editable = new LinkedHashMap<>();
                editable.put("Ingrediente", fila.getOrDefault("Ingrediente", ""));
                editable.put("Nombre Ingrediente", fila.getOrDefault("Nombre Ingrediente", ""));
                editable.put("Cantidad Base", String.format("%.4f", originalCantidad));
                editable.put("Cantidad", String.format("%.4f", nuevaCantidad));
                editable.put("Unidades", fila.getOrDefault("Unidad", ""));
                editable.put("Costo", String.format("%.4f", nuevoCosto));
                editable.put("Costo Unitario", String.format("%.4f", costoUnitario));
                editable.put("Check", "✓".equals(fila.getOrDefault("Incluye", "")) ? "✓" : " ");
                datos.add(editable);
            }

        } else {
            List<Map<String, String>> receta = VerUtils.verFilas("Recetas", Map.of("Código receta", codigoReceta));
            double rendimiento = receta.isEmpty() ? 0 : parseDouble(receta.get(0).getOrDefault("Rendimiento", "0"));
            if (rendimiento <= 0) {
                VBox errorBox = new VBox(PanelIngredientesEstiloUtils.crearLabelError("❌ No se encontró rendimiento para la receta."));
                errorBox.setStyle("-fx-padding: 20; -fx-background-color: #FFF3E0; -fx-border-color: red;");
                return errorBox;
            }

            double factor = cantidadActual / rendimiento;
            System.out.printf("📐 No había ProduccionIngredientes. Escalando desde RecetasIngredientes con factor: %.4f%n", factor);

            if (factor == 0) {
                System.out.println("⚠️ Factor de escalado = 0. No se recalcularán ingredientes.");
                return new VBox(new Label("⚠️ No se pudo escalar los ingredientes. Factor 0."));
            }

            List<Map<String, String>> base = VerUtils.verFilas("RecetasIngredientes", Map.of("Código receta", codigoReceta));

            for (Map<String, String> fila : base) {
                double cantidadOriginal = parseDouble(fila.getOrDefault("Cantidad", "0"));
                String unidad = fila.getOrDefault("Unidades", "");
                String ingrediente = fila.getOrDefault("Ingrediente", "");

                double nuevaCantidad = cantidadOriginal * factor;
                double costoUnitario = calcularCostoUnitario(ingrediente, unidad);
                double costoTotal = nuevaCantidad * costoUnitario;

                Map<String, String> map = new LinkedHashMap<>();
                map.put("Ingrediente", ingrediente);
                map.put("Nombre Ingrediente", mapaNombreIngredientes.getOrDefault(ingrediente, ""));
                map.put("Cantidad Base", String.format("%.4f", cantidadOriginal));
                map.put("Cantidad", String.format("%.4f", nuevaCantidad));
                map.put("Unidades", unidad);
                map.put("Costo", String.format("%.4f", costoTotal));
                map.put("Costo Unitario", String.format("%.4f", costoUnitario));
                map.put("Check", "✓");
                datos.add(map);
            }
        }

        // LOG adicional para verificar qué se cargó
        System.out.println("📦 Ingredientes escalados generados:");
        for (Map<String, String> fila : datos) {
            System.out.printf("   - [%s] %s %s → Costo: %s%n",
                    fila.getOrDefault("Ingrediente", "?"),
                    fila.getOrDefault("Cantidad", "?"),
                    fila.getOrDefault("Unidades", "?"),
                    fila.getOrDefault("Costo", "?"));
        }

        VBox tabla = PanelIngredientesTablaFactory.crearTabla(
                datos,
                mapaNombreIngredientes,
                produccion,
                codigoProduccion,
                actualizarCostoEnTabla
        );

        VBox contenedor = new VBox(10, new Label("Ingredientes registrados:"), tabla);
        contenedor.setPrefWidth(500);
        contenedor.setStyle("-fx-padding: 20; -fx-background-color: #FFFDE7; -fx-background-radius: 10;");
        PanelRecargaIngredientesUtils.inicializar(
            contenedor, datos, produccion, mapaNombreIngredientes, actualizarCostoEnTabla
        );

        return contenedor;
    }

    private static double calcularCostoUnitario(String codIngrediente, String unidadDestino) {
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");
        Map<String, String> fila = ingredientes.stream()
                .filter(i -> codIngrediente.equalsIgnoreCase(i.get("Código")))
                .findFirst()
                .orElse(null);

        if (fila == null) return 0;

        String unidadBase = fila.getOrDefault("Unidad", "").trim();
        double precioBase = parseDouble(fila.getOrDefault("Precio Local", "0"));

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadBase, unidadDestino, 1.0, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida == 0) return 0;

        return precioBase / cantidadConvertida;
    }

    private static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseInt(String val) {
        try {
            return Integer.parseInt(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
