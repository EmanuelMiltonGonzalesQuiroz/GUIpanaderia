package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ExcelDataUtils {

    public static List<String> obtenerUnicos(String tabla, String columna) {
        Set<String> valores = new TreeSet<>();
        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook wb = new XSSFWorkbook(file)) {

            XSSFSheet hoja = buscarHoja(wb, tabla);
            if (hoja == null) return List.of();

            XSSFTable t = hoja.getTables().stream()
                    .filter(x -> x.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (t == null) return List.of();

            AreaReference area = new AreaReference(t.getArea().formatAsString(), wb.getSpreadsheetVersion());
            Row head = hoja.getRow(area.getFirstCell().getRow());

            int col = -1;
            for (int i = area.getFirstCell().getCol(); i <= area.getLastCell().getCol(); i++) {
                if (head.getCell(i).getStringCellValue().equalsIgnoreCase(columna)) {
                    col = i; break;
                }
            }

            if (col == -1) return List.of();

            for (int r = area.getFirstCell().getRow() + 1; r <= area.getLastCell().getRow(); r++) {
                Row fila = hoja.getRow(r);
                if (fila == null) continue;
                Cell c = fila.getCell(col);
                if (c != null) valores.add(c.toString().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(valores);
    }

    public static XSSFSheet buscarHoja(XSSFWorkbook wb, String tabla) {
        for (Sheet s : wb) {
            if (s instanceof XSSFSheet xs) {
                for (XSSFTable t : xs.getTables()) {
                    if (t.getName().equalsIgnoreCase(tabla)) return xs;
                }
            }
        }
        return null;
    }
    public static List<Map<String, String>> obtenerTabla(String tabla) {
        List<Map<String, String>> lista = new ArrayList<>();
        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook wb = new XSSFWorkbook(file)) {
    
            XSSFSheet hoja = ExcelUtils.buscarHojaConTabla(wb, tabla);
            if (hoja == null) return lista;
    
            XSSFTable t = hoja.getTables().stream()
                    .filter(x -> x.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (t == null) return lista;
    
            AreaReference area = new AreaReference(t.getArea().formatAsString(), wb.getSpreadsheetVersion());
            int filaInicio = area.getFirstCell().getRow();
            int filaFin = area.getLastCell().getRow();
            int colInicio = area.getFirstCell().getCol();
            int colFin = area.getLastCell().getCol();
    
            Row encabezado = hoja.getRow(filaInicio);
            List<String> columnas = new ArrayList<>();
            for (int i = colInicio; i <= colFin; i++) {
                columnas.add(encabezado.getCell(i).getStringCellValue().trim());
            }
    
            for (int i = filaInicio + 1; i <= filaFin; i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
    
                boolean vacía = true;
                Map<String, String> filaMap = new LinkedHashMap<>();
                for (int j = colInicio; j <= colFin; j++) {
                    Cell celda = fila.getCell(j);
                    String val = (celda != null) ? celda.toString().trim() : "";
                    if (!val.isEmpty()) vacía = false;
                    filaMap.put(columnas.get(j - colInicio), val);
                }
                if (!vacía) lista.add(filaMap);
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    
}
