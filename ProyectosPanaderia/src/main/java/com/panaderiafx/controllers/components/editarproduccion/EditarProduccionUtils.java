package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.utils.ConversorMezclaUtils;
import com.panaderiafx.utils.EliminarUtils;
import com.panaderiafx.utils.ModificarUtils;
import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class EditarProduccionUtils {

    private static final String RUTA = "Datos\\Hoja de datos.xlsx";

    public static void editarProduccionYIngredientes(String codigoProduccion,
                                                     TextField campoFecha,
                                                     TextField campoCantidad,
                                                     TextField campoPrecioU,
                                                     TextField campoMezcla,
                                                     TextField campoProducto) {
        if (codigoProduccion == null || codigoProduccion.isBlank()) {
            mostrarError("Código de producción no válido.");
            return;
        }

        // === 1. Leer datos actuales
        Map<String, String> filaProd = VerUtils.verFila("Produccion", Map.of("Código Producción", codigoProduccion));
        if (filaProd == null) {
            mostrarError("Producción no encontrada.");
            return;
        }

        // === 2. Calcular campos derivados
        double cantidad = parseDouble(campoCantidad.getText());
        double precioUnit = parseDouble(campoPrecioU.getText());
        String codReceta = filaProd.get("Código Receta");
        double rendimiento = ConversorMezclaUtils.obtenerRendimientoReceta(codReceta);

        double factor = (rendimiento > 0) ? cantidad / rendimiento : 0;
        double costoTotal = 0.0;

        List<Map<String, String>> ingredientes = VerUtils.verFilas("ProduccionIngredientes", Map.of("Código Producción", codigoProduccion));
        for (Map<String, String> ingrediente : ingredientes) {
            String codIng = ingrediente.get("Ingrediente");
            double baseCantidad = parseDouble(ingrediente.getOrDefault("Cantidad Base", ingrediente.getOrDefault("Cantidad Usada", "0")));
            double nuevaCantidad = baseCantidad * factor;
            double costoUnitario = parseDouble(ingrediente.getOrDefault("Costo Unitario", "0"));
            double nuevoCosto = nuevaCantidad * costoUnitario;
            costoTotal += nuevoCosto;

            Map<String, String> nuevosValoresIng = new LinkedHashMap<>();
            nuevosValoresIng.put("Cantidad Usada", String.format("%.4f", nuevaCantidad));
            nuevosValoresIng.put("Costo Total", String.format("%.4f", nuevoCosto));

            ModificarUtils.modificarFila("ProduccionIngredientes", Map.of(
                    "Código Producción", codigoProduccion,
                    "Ingrediente", codIng
            ), nuevosValoresIng);
        }

        // === 3. Actualizar la fila de Producción
        double costoU = (cantidad > 0) ? costoTotal / cantidad : 0.0;
        double gananciaTotal = (precioUnit * cantidad) - costoTotal;

        Map<String, String> nuevosValores = Map.of(
                "Fecha", campoFecha.getText().trim(),
                "Cantidad Producida", campoCantidad.getText().trim(),
                "Precio de Venta por Unidad", campoPrecioU.getText().trim(),
                "Mezcla", campoMezcla.getText().trim(),
                "Producto", campoProducto.getText().trim(),
                "Costo Total", String.format("%.2f", costoTotal),
                "Costo Directo/U", String.format("%.4f", costoU),
                "Ganancia Tota", String.format("%.2f", gananciaTotal)
        );

        boolean actualizado = ModificarUtils.modificarFila("Produccion", Map.of("Código Producción", codigoProduccion), nuevosValores);

        if (actualizado) {
            mostrarConfirmacion("✅ Producción e ingredientes actualizados correctamente.");
        } else {
            mostrarError("No se pudo actualizar la producción.");
        }
    }

    public static void eliminarProduccionCompleta(String codigoProduccion) {
        if (codigoProduccion == null || codigoProduccion.isBlank()) {
            mostrarError("Código de producción no válido.");
            return;
        }

        boolean eliminadoProd = EliminarUtils.eliminarFila("Produccion", Map.of("Código Producción", codigoProduccion));
        int eliminados = eliminarFilasProduccionIngredientes(codigoProduccion);

        if (eliminadoProd || eliminados > 0) {
            mostrarConfirmacion("🗑 Producción y " + eliminados + " ingredientes eliminados correctamente.");
        } else {
            mostrarError("No se pudo eliminar la producción ni sus ingredientes.");
        }
    }

    private static int eliminarFilasProduccionIngredientes(String codigoProduccion) {
        int eliminados = 0;
        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet hoja = libro.getSheet("ProduccionIngredientes");
            if (hoja == null) return 0;

            Row headerRow = hoja.getRow(0);
            Map<String, Integer> columnas = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell celda = headerRow.getCell(c);
                if (celda != null) {
                    columnas.put(celda.getStringCellValue().trim(), c);
                }
            }

            List<Integer> filasAEliminar = new ArrayList<>();

            for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                Row fila = hoja.getRow(f);
                if (fila == null) continue;

                int col = columnas.getOrDefault("Código Producción", -1);
                if (col == -1) continue;
                Cell celda = fila.getCell(col);
                String contenido = celda != null ? celda.toString().trim() : "";
                if (codigoProduccion.equalsIgnoreCase(contenido)) {
                    filasAEliminar.add(f);
                }
            }

            Collections.reverse(filasAEliminar);
            for (int f : filasAEliminar) {
                hoja.removeRow(hoja.getRow(f));
                if (f < hoja.getLastRowNum()) {
                    hoja.shiftRows(f + 1, hoja.getLastRowNum(), -1);
                }
                eliminados++;
            }

            try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                libro.write(fos);
            }

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar ingredientes: " + e.getMessage());
        }

        return eliminados;
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

    private static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
