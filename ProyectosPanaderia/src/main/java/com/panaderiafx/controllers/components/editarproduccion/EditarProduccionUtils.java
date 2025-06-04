package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.utils.EliminarUtils;
import com.panaderiafx.utils.ModificarUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.*;

public class EditarProduccionUtils {

    public static void editarProduccionYIngredientes(String codigoProduccion,
                                                     TextField campoFecha,
                                                     TextField campoCantidad,
                                                     TextField campoPrecioU,
                                                     TextField campoMezcla,
                                                     TextField campoProducto,
                                                     ObservableList<Map<String, String>> ingredientesActuales) {
        if (codigoProduccion == null || codigoProduccion.isBlank()) {
            mostrarError("Código de producción no válido.");
            return;
        }

        Map<String, String> filaProd = VerUtils.verFila("Produccion", Map.of("Código Producción", codigoProduccion));
        if (filaProd == null) {
            mostrarError("Producción no encontrada.");
            return;
        }

        double cantidad = ParseUtils.toDouble(campoCantidad.getText());
        double precioUnit = ParseUtils.toDouble(campoPrecioU.getText());
        double costoTotal = 0.0;

        for (Map<String, String> fila : ingredientesActuales) {
            String codIng = fila.getOrDefault("Ingrediente", "").trim();
            String cantidadStr = fila.getOrDefault("Cantidad", "0").trim();
            String costoStr = fila.getOrDefault("Costo", "0").trim();
            String incluye = fila.getOrDefault("Check", "✓").trim();

            double cantidadUsada = ParseUtils.toDouble(cantidadStr);
            double costoTotalIng = ParseUtils.toDouble(costoStr);

            Map<String, String> nuevosValoresIng = new LinkedHashMap<>();
            nuevosValoresIng.put("Cantidad Usada", String.format("%.4f", cantidadUsada));
            nuevosValoresIng.put("Costo Total", String.format("%.4f", costoTotalIng));
            nuevosValoresIng.put("Incluye", incluye);
            nuevosValoresIng.put("Fecha Registro", campoFecha.getText().trim());

            boolean modificado = ModificarUtils.modificarFila(
                    "ProduccionIngredientes",
                    Map.of("Código Producción", codigoProduccion, "Ingrediente", codIng),
                    nuevosValoresIng
            );

            if (modificado) {
                System.out.println("🧾 Ingrediente [" + codIng + "] modificado.");
            }

            if ("✓".equals(incluye)) {
                costoTotal += costoTotalIng;
            }
        }

        double costoU = cantidad > 0 ? costoTotal / cantidad : 0.0;
        double gananciaTotal = (precioUnit * cantidad) - costoTotal;

        Map<String, String> nuevosValores = Map.of(
                "Fecha", campoFecha.getText().trim(),
                "Cantidad Producida", campoCantidad.getText().trim(),
                "Precio de Venta por Unidad", campoPrecioU.getText().trim(),
                "Mezcla", campoMezcla.getText().trim(),
                "Producto", campoProducto.getText().trim(),
                "Costo Total", String.format("%.2f", costoTotal),
                "Costo Directo/U", String.format("%.4f", costoU),
                "Ganancia Total", String.format("%.2f", gananciaTotal)
        );

        boolean actualizado = ModificarUtils.modificarFila(
                "Produccion",
                Map.of("Código Producción", codigoProduccion),
                nuevosValores
        );

        if (actualizado) {
            mostrarConfirmacion("✅ Producción e ingredientes actualizados correctamente.");
            VerUtils.refrescarExcel();
        } else {
            mostrarError("No se pudo actualizar la producción.");
        }
    }

    public static void eliminarProduccionCompleta(String codigoProduccion) {
        if (codigoProduccion == null || codigoProduccion.isBlank()) {
            mostrarError("Código de producción no válido.");
            return;
        }

        long tInicio = System.currentTimeMillis();

        int eliminadosIngredientes = EliminarUtils.eliminarFilas("ProduccionIngredientes", Map.of(
                "Código Producción", codigoProduccion));
        boolean eliminadoProduccion = EliminarUtils.eliminarFila("Produccion", Map.of(
                "Código Producción", codigoProduccion));

        VerUtils.refrescarExcel();

        if (eliminadoProduccion || eliminadosIngredientes > 0) {
            System.out.printf("✅ Producción y %d ingrediente(s) eliminados. ⏱️ %d ms%n",
                    eliminadosIngredientes, System.currentTimeMillis() - tInicio);
            mostrarConfirmacion("🗑 Producción y " + eliminadosIngredientes + " ingrediente(s) eliminados.");
        } else {
            mostrarError("❌ No se pudo eliminar la producción.");
        }
    }

    private static void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
