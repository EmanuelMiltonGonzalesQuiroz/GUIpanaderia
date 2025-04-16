package com.panaderiafx.utils;

import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CrearUtils {

    private static final String RUTA = "C:\\Panaderia\\Datos\\Hoja de datos.xlsx";

    public static boolean crearFila(String nombreTabla, Map<String, String> nuevaFila) {
        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {
    
            Sheet hoja = libro.getSheet(nombreTabla); // Usa directamente el nombre de la hoja
            if (hoja == null) {
                System.err.println("❌ Hoja '" + nombreTabla + "' no encontrada.");
                return false;
            }
    
            Row headerRow = hoja.getRow(0); // Primera fila como cabecera
            Map<String, Integer> columnas = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell celda = headerRow.getCell(c);
                if (celda != null) {
                    columnas.put(celda.getStringCellValue().trim(), c);
                }
            }
    
            // Buscar primera fila vacía
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
                }
            }
    
            try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                libro.write(fos);
                return true;
            }
    
        } catch (Exception e) {
            System.err.println("❌ Error al crear fila: " + e.getMessage());
        }
        return false;
    }
    
    private static boolean filaIsEmpty(Row fila, int colInicio, int colTotal) {
        for (int c = colInicio; c < colInicio + colTotal; c++) {
            Cell celda = fila.getCell(c);
            if (celda != null && !celda.toString().trim().isEmpty()) return false;
        }
        return true;
    }
}
