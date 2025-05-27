package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class EliminarUtils {

    private static final String RUTA = "Datos\\Hoja de datos.xlsx";

    public static boolean eliminarFila(String nombreTabla, Map<String, String> condiciones) {
        return eliminarFilas(nombreTabla, condiciones) > 0;
    }

    public static int eliminarFilas(String nombreTabla, Map<String, String> condiciones) {
        long inicio = System.currentTimeMillis();
        int eliminados = 0;

        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet hoja = libro.getSheet(nombreTabla);
            if (hoja == null) {
                System.err.println("❌ Hoja '" + nombreTabla + "' no encontrada.");
                return 0;
            }

            Row headerRow = hoja.getRow(0);
            Map<String, Integer> columnas = obtenerMapaColumnas(headerRow);

            List<List<String>> filasConservadas = new ArrayList<>();
            filasConservadas.add(obtenerValoresFila(headerRow)); // Encabezado

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

                if (!coincide) {
                    filasConservadas.add(obtenerValoresFila(fila));
                } else {
                    eliminados++;
                }
            }

            // Limpiar hoja desde la fila 1 hacia abajo
            for (int i = hoja.getLastRowNum(); i >= 1; i--) {
                Row fila = hoja.getRow(i);
                if (fila != null) hoja.removeRow(fila);
            }

            // Reescribir filas conservadas
            for (int i = 1; i < filasConservadas.size(); i++) {
                Row fila = hoja.createRow(i);
                List<String> valores = filasConservadas.get(i);
                for (int j = 0; j < valores.size(); j++) {
                    fila.createCell(j).setCellValue(valores.get(j));
                }
            }

            // Guardar los cambios
            try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                libro.write(fos);
            }

            long tiempo = System.currentTimeMillis() - inicio;
            System.out.printf("🗑 Se eliminaron %d fila(s) de '%s' con condiciones: %s ⏱️ %d ms%n",
                    eliminados, nombreTabla, condiciones, tiempo);

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar filas: " + e.getMessage());
            e.printStackTrace();
        }

        return eliminados;
    }

    private static List<String> obtenerValoresFila(Row fila) {
        List<String> valores = new ArrayList<>();
        for (int c = 0; c < fila.getLastCellNum(); c++) {
            Cell celda = fila.getCell(c);
            valores.add(celda == null ? "" : celda.toString().trim());
        }
        return valores;
    }

    private static Map<String, Integer> obtenerMapaColumnas(Row headerRow) {
        Map<String, Integer> columnas = new LinkedHashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell celda = headerRow.getCell(c);
            if (celda != null) {
                columnas.put(celda.getStringCellValue().trim(), c);
            }
        }
        return columnas;
    }
}
