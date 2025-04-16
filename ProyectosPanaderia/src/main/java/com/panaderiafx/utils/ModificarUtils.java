package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ModificarUtils {

    private static final String RUTA = "C:\\Panaderia\\Datos\\Hoja de datos.xlsx";

    public static boolean modificarFila(String nombreTabla, Map<String, String> condiciones, Map<String, String> nuevosValores) {
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
                    for (Map.Entry<String, String> cambio : nuevosValores.entrySet()) {
                        if (columnas.containsKey(cambio.getKey())) {
                            int col = columnas.get(cambio.getKey());
                            Cell celda = fila.getCell(col);
                            if (celda == null) celda = fila.createCell(col);
                            celda.setCellValue(cambio.getValue());
                        }
                    }
                    try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                        libro.write(fos);
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al modificar fila: " + e.getMessage());
        }

        return false;
    }
}
