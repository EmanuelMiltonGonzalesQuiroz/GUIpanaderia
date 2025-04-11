package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class VerUtils {

    private static final String RUTA_EXCEL = "C:\\Excel\\Datos\\Hoja de datos.xlsx";

    public static List<Map<String, String>> verTabla(String nombreTabla) {
        List<Map<String, String>> datos = new ArrayList<>();

        try (InputStream fis = new FileInputStream(RUTA_EXCEL);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet sheet = null;

            // 1. Buscar si existe una tabla con nombre definido
            for (Name namedRange : libro.getAllNames()) {
                if (namedRange.getNameName().equalsIgnoreCase(nombreTabla)) {
                    AreaReference area = new AreaReference(namedRange.getRefersToFormula(), libro.getSpreadsheetVersion());
                    CellReference[] celdas = area.getAllReferencedCells();
                    sheet = libro.getSheet(celdas[0].getSheetName());

                    int filaInicio = celdas[0].getRow();
                    int filaFin = celdas[celdas.length - 1].getRow();
                    int colInicio = celdas[0].getCol();
                    int colFin = celdas[celdas.length - 1].getCol();

                    Row filaEncabezado = sheet.getRow(filaInicio);
                    List<String> encabezados = new ArrayList<>();
                    for (int c = colInicio; c <= colFin; c++) {
                        Cell celda = filaEncabezado.getCell(c);
                        encabezados.add(celda != null ? celda.toString().trim() : "");
                    }

                    for (int f = filaInicio + 1; f <= filaFin; f++) {
                        Row fila = sheet.getRow(f);
                        if (fila == null) continue;

                        Map<String, String> filaMap = new LinkedHashMap<>();
                        boolean vacia = true;

                        for (int c = colInicio; c <= colFin; c++) {
                            Cell celda = fila.getCell(c);
                            String valor = celda != null ? celda.toString().trim() : "";
                            if (!valor.isEmpty()) vacia = false;
                            filaMap.put(encabezados.get(c - colInicio), valor);
                        }

                        if (!vacia) datos.add(filaMap);
                    }

                    return datos;
                }
            }

            // 2. Si no hay rango con nombre, buscar hoja con ese nombre
            sheet = libro.getSheet(nombreTabla);
            if (sheet != null) {
                Iterator<Row> filas = sheet.iterator();
                if (!filas.hasNext()) return datos;

                Row encabezado = filas.next();
                List<String> columnas = new ArrayList<>();
                for (Cell celda : encabezado) {
                    columnas.add(celda.toString().trim());
                }

                while (filas.hasNext()) {
                    Row fila = filas.next();
                    Map<String, String> filaMap = new LinkedHashMap<>();
                    boolean vacia = true;

                    for (int i = 0; i < columnas.size(); i++) {
                        Cell celda = fila.getCell(i);
                        String valor = celda != null ? celda.toString().trim() : "";
                        if (!valor.isEmpty()) vacia = false;
                        filaMap.put(columnas.get(i), valor);
                    }

                    if (!vacia) datos.add(filaMap);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al leer tabla u hoja " + nombreTabla + ": " + e.getMessage());
        }

        return datos;
    }

    public static List<String> verColumna(String nombreTabla, String columna) {
        return verTabla(nombreTabla).stream()
                .map(fila -> fila.getOrDefault(columna, ""))
                .toList();
    }

    public static Map<String, String> verFila(String nombreTabla, Map<String, String> filtros) {
        return verTabla(nombreTabla).stream()
                .filter(fila -> filtros.entrySet().stream()
                        .allMatch(f -> f.getValue().equalsIgnoreCase(fila.getOrDefault(f.getKey(), ""))))
                .findFirst()
                .orElse(null);
    }

    public static List<Map<String, String>> verFilas(String nombreTabla, Map<String, String> filtros) {
        return verTabla(nombreTabla).stream()
                .filter(fila -> filtros.entrySet().stream()
                        .allMatch(f -> f.getValue().equalsIgnoreCase(fila.getOrDefault(f.getKey(), ""))))
                .toList();
    }
}
