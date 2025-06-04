package com.panaderiafx.utils;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CrearUtils {

    private static final String RUTA = "Datos\\Hoja de datos.xlsx";

    public static synchronized boolean crearFila(String nombreTabla, Map<String, String> nuevaFila) {
        System.out.println("🟢 Intentando crear fila en hoja: " + nombreTabla);

        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet hoja = libro.getSheet(nombreTabla);
            if (hoja == null) {
                System.err.println("❌ Hoja '" + nombreTabla + "' no encontrada.");
                return false;
            }

            Row headerRow = hoja.getRow(0);
            if (headerRow == null) {
                System.err.println("❌ Fila de encabezado vacía.");
                return false;
            }

            Map<String, Integer> columnas = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell celda = headerRow.getCell(c);
                if (celda != null) {
                    columnas.put(celda.getStringCellValue().trim(), c);
                }
            }

            // Generar código automático si corresponde
            for (String clave : nuevaFila.keySet()) {
                if ("Auto".equalsIgnoreCase(nuevaFila.get(clave))) {
                    String nuevoCodigo = generarCodigo(nombreTabla, clave, hoja, columnas.get(clave));
                    nuevaFila.put(clave, nuevoCodigo);
                }
            }

            // Buscar fila vacía
            int filaDestino = hoja.getLastRowNum() + 1;
            for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                Row fila = hoja.getRow(f);
                if (fila == null || filaIsEmpty(fila, 0, columnas.size())) {
                    filaDestino = f;
                    break;
                }
            }

            Row nueva = hoja.createRow(filaDestino);
            for (Map.Entry<String, String> entry : nuevaFila.entrySet()) {
                if (columnas.containsKey(entry.getKey())) {
                    int col = columnas.get(entry.getKey());
                    Cell celda = nueva.createCell(col);
                    celda.setCellValue(entry.getValue());
                    System.out.printf("   ➤ [%s] columna %d ➝ %s%n", entry.getKey(), col, entry.getValue());
                }
            }

            try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                libro.write(fos);
                fos.flush();
            }

            System.out.printf("✅ Fila creada correctamente en hoja '%s'.%n", nombreTabla);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al crear fila: " + e.getMessage());
        }
        VerUtils.refrescarExcel();
        return false;
    }

    private static boolean filaIsEmpty(Row fila, int colInicio, int colTotal) {
        for (int c = colInicio; c < colInicio + colTotal; c++) {
            Cell celda = fila.getCell(c);
            if (celda != null && !celda.toString().trim().isEmpty()) return false;
        }
        return true;
    }

    private static String generarCodigo(String tabla, String campo, Sheet hoja, int col) {
        int max = 0;
        for (int r = 1; r <= hoja.getLastRowNum(); r++) {
            Row fila = hoja.getRow(r);
            if (fila == null) continue;

            Cell celda = fila.getCell(col);
            if (celda == null) continue;

            String valor = celda.toString().trim();
            if (valor.matches("[A-Z]+\\d+")) {
                String num = valor.replaceAll("[^0-9]", "");
                try {
                    max = Math.max(max, Integer.parseInt(num));
                } catch (NumberFormatException ignored) {}
            }
        }

        String prefijo = tabla.equalsIgnoreCase("RecetasVersion") ? "VER" :
                         tabla.equalsIgnoreCase("Ingredientes") ? "ING" : "COD";

        return prefijo + String.format("%03d", max + 1);
    }
}