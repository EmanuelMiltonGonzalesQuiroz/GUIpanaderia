package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

public class ExcelUtils {

    public static List<String> obtenerColumnaUnica(String tabla, String columna) {
        List<String> valores = new ArrayList<>();
        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(file)) {

            XSSFSheet hoja = buscarHojaConTabla(workbook, tabla);
            if (hoja == null) return List.of();

            XSSFTable table = hoja.getTables().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (table == null) return List.of();

            AreaReference area = new AreaReference(table.getArea().formatAsString(), workbook.getSpreadsheetVersion());
            Row encabezado = hoja.getRow(area.getFirstCell().getRow());

            int columnaIndex = -1;
            for (int i = area.getFirstCell().getCol(); i <= area.getLastCell().getCol(); i++) {
                Cell celda = encabezado.getCell(i);
                if (celda.getStringCellValue().equalsIgnoreCase(columna)) {
                    columnaIndex = i;
                    break;
                }
            }

            if (columnaIndex == -1) return List.of();

            Set<String> set = new LinkedHashSet<>();
            for (int i = area.getFirstCell().getRow() + 1; i <= area.getLastCell().getRow(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
                Cell celda = fila.getCell(columnaIndex);
                if (celda != null && !celda.toString().isBlank()) {
                    set.add(celda.toString().trim());
                }
            }

            valores.addAll(set);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return valores;
    }
    public static boolean agregarAFilaVacia(String tabla, Map<String, String> datos) {
        try {
            return guardarEnTabla(tabla, datos);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public static boolean guardarEnTabla(String tabla, Map<String, String> datos) {
        String ruta = "C:\\Excel\\Datos\\Hoja de datos.xlsx";
        try (FileInputStream fis = new FileInputStream(ruta);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
    
            XSSFSheet hoja = buscarHojaConTabla(wb, tabla);
            if (hoja == null) return false;
    
            XSSFTable t = hoja.getTables().stream()
                    .filter(x -> x.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (t == null) return false;
    
            AreaReference area = new AreaReference(t.getArea().formatAsString(), wb.getSpreadsheetVersion());
            int filaInicio = area.getFirstCell().getRow();
            int filaFin = area.getLastCell().getRow();
            int colInicio = area.getFirstCell().getCol();
            int colFin = area.getLastCell().getCol();
    
            Row encabezado = hoja.getRow(filaInicio);
            List<String> columnas = new ArrayList<>();
            for (int i = colInicio; i <= colFin; i++) {
                Cell celda = encabezado.getCell(i);
                columnas.add(celda != null ? celda.getStringCellValue().trim() : "");
            }
    
            // Buscar primera fila completamente vacía
            int filaDestino = -1;
            for (int i = filaInicio + 1; i <= filaFin; i++) {
                Row fila = hoja.getRow(i);
                if (fila == null || fila.toString().isBlank()) {
                    filaDestino = i;
                    break;
                }
                boolean vacía = true;
                for (int j = colInicio; j <= colFin; j++) {
                    Cell celda = fila.getCell(j);
                    if (celda != null && !celda.toString().isBlank()) {
                        vacía = false;
                        break;
                    }
                }
                if (vacía) {
                    filaDestino = i;
                    break;
                }
            }
    
            if (filaDestino == -1) return false;
            Row filaNueva = hoja.getRow(filaDestino);
            if (filaNueva == null) filaNueva = hoja.createRow(filaDestino);
    
            // Escribir datos en columnas correctas
            for (int i = 0; i < columnas.size(); i++) {
                String nombreCol = columnas.get(i);
                String valor = datos.getOrDefault(nombreCol, "");
                Cell celda = filaNueva.createCell(colInicio + i);
                try {
                    double val = Double.parseDouble(valor);
                    celda.setCellValue(val);
                } catch (NumberFormatException e) {
                    celda.setCellValue(valor);
                }
            }
    
            fis.close();
            try (FileOutputStream fos = new FileOutputStream(ruta)) {
                wb.write(fos);
                return true;
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static XSSFSheet buscarHojaConTabla(XSSFWorkbook wb, String nombreTabla) {
        for (Sheet s : wb) {
            if (s instanceof XSSFSheet xs) {
                for (XSSFTable t : xs.getTables()) {
                    if (t.getName().equalsIgnoreCase(nombreTabla)) return xs;
                }
            }
        }
        return null;
    }
  
    public static boolean modificarPorCodigo(String tabla, String codigo, Map<String, String> nuevosValores) {
        try (FileInputStream fis = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
    
            XSSFSheet hoja = buscarHojaConTabla(workbook, tabla);
            if (hoja == null) return false;
    
            XSSFTable table = hoja.getTables().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (table == null) return false;
    
            AreaReference area = new AreaReference(table.getArea().formatAsString(), workbook.getSpreadsheetVersion());
            int filaInicio = area.getFirstCell().getRow();
            int filaFin = area.getLastCell().getRow();
            int colInicio = area.getFirstCell().getCol();
            int colFin = area.getLastCell().getCol();
    
            Row encabezado = hoja.getRow(filaInicio);
            Map<String, Integer> columnas = new HashMap<>();
            for (int i = colInicio; i <= colFin; i++) {
                String nombre = encabezado.getCell(i).getStringCellValue().trim();
                columnas.put(nombre.toLowerCase(), i);
            }
    
            for (int r = filaInicio + 1; r <= filaFin; r++) {
                Row fila = hoja.getRow(r);
                if (fila == null) continue;
                Cell celdaCodigo = fila.getCell(columnas.get("codigo"));
                if (celdaCodigo == null) continue;
    
                String valorCodigo = celdaCodigo.toString().trim();
                if (valorCodigo.equalsIgnoreCase(codigo)) {
                    for (Map.Entry<String, String> e : nuevosValores.entrySet()) {
                        String col = e.getKey().trim().toLowerCase();
                        if (columnas.containsKey(col)) {
                            Cell celda = fila.getCell(columnas.get(col));
                            if (celda == null) celda = fila.createCell(columnas.get(col));
                            celda.setCellValue(e.getValue());
                        }
                    }
    
                    try (var out = new java.io.FileOutputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx")) {
                        workbook.write(out);
                    }
    
                    return true;
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean eliminarPorCodigo(String tabla, String codigo) {
        try (FileInputStream fis = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
    
            XSSFSheet hoja = buscarHojaConTabla(workbook, tabla);
            if (hoja == null) return false;
    
            XSSFTable table = hoja.getTables().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (table == null) return false;
    
            AreaReference area = new AreaReference(table.getArea().formatAsString(), workbook.getSpreadsheetVersion());
            int filaInicio = area.getFirstCell().getRow();
            int filaFin = area.getLastCell().getRow();
            int colInicio = area.getFirstCell().getCol();
            int colFin = area.getLastCell().getCol();
    
            Row encabezado = hoja.getRow(filaInicio);
            Map<String, Integer> columnas = new HashMap<>();
            for (int i = colInicio; i <= colFin; i++) {
                String nombre = encabezado.getCell(i).getStringCellValue().trim();
                columnas.put(nombre.toLowerCase(), i);
            }
    
            for (int r = filaInicio + 1; r <= filaFin; r++) {
                Row fila = hoja.getRow(r);
                if (fila == null) continue;
                Cell celdaCodigo = fila.getCell(columnas.get("codigo"));
                if (celdaCodigo == null) continue;
    
                String valorCodigo = celdaCodigo.toString().trim();
                if (valorCodigo.equalsIgnoreCase(codigo)) {
                    // Limpiar toda la fila
                    for (int j = colInicio; j <= colFin; j++) {
                        Cell celda = fila.getCell(j);
                        if (celda != null) fila.removeCell(celda);
                    }
    
                    try (var out = new java.io.FileOutputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx")) {
                        workbook.write(out);
                    }
    
                    return true;
                }
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }    
    
    
}
