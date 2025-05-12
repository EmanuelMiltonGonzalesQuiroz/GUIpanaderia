package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class EliminarUtils {

    private static final String RUTA = "Datos\\Hoja de datos.xlsx";

    public static boolean eliminarFila(String nombreTabla, Map<String, String> condiciones) {
        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet hoja = libro.getSheet(nombreTabla);
            if (hoja == null) {
                System.err.println("❌ Hoja '" + nombreTabla + "' no encontrada.");
                return false;
            }

            Row headerRow = hoja.getRow(0);
            Map<String, Integer> columnas = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell celda = headerRow.getCell(c);
                if (celda != null) {
                    columnas.put(celda.getStringCellValue().trim(), c);
                }
            }

            for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                Row fila = hoja.getRow(f);
                if (fila == null) continue;

                boolean coincide = condiciones.entrySet().stream()
                        .allMatch(e -> {
                            int col = columnas.getOrDefault(e.getKey(), -1);
                            if (col == -1) return false;
                            Cell celda = fila.getCell(col);
                            return celda != null && e.getValue().equalsIgnoreCase(celda.toString().trim());
                        });

                if (coincide) {
                    // Eliminar físicamente
                    hoja.removeRow(fila);
                    if (f < hoja.getLastRowNum()) {
                        hoja.shiftRows(f + 1, hoja.getLastRowNum(), -1);
                    }

                    try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                        libro.write(fos);
                        System.out.println("🗑 Fila eliminada correctamente de: " + nombreTabla);
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar fila: " + e.getMessage());
        }

        return false;
    }
}
