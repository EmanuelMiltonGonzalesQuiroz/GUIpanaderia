package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class CodigoGenerator {

    public static String generar(String tabla) {
        String prefijo = switch (tabla.toLowerCase()) {
            case "recetas" -> "REC";
            case "costos" -> "COS";
            case "tasacambio" -> "TSC";
            case "tablaconversion" -> "CON";
            default -> "ING";
        };

        Set<String> existentes = obtenerCodigos(tabla);
        for (int i = 1; i < 1000; i++) {
            String codigo = String.format("%s%03d", prefijo, i);
            if (!existentes.contains(codigo)) return codigo;
        }
        return prefijo + "999";
    }

    private static Set<String> obtenerCodigos(String tabla) {
        Set<String> codigos = new HashSet<>();
        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook wb = new XSSFWorkbook(file)) {

            for (Sheet s : wb) {
                if (s instanceof XSSFSheet xs) {
                    for (XSSFTable t : xs.getTables()) {
                        if (t.getName().equalsIgnoreCase(tabla)) {
                            AreaReference area = new AreaReference(t.getArea().formatAsString(), wb.getSpreadsheetVersion());
                            int col = area.getFirstCell().getCol();
                            for (int i = area.getFirstCell().getRow() + 1; i <= area.getLastCell().getRow(); i++) {
                                Row row = xs.getRow(i);
                                if (row == null) continue;
                                Cell c = row.getCell(col);
                                if (c != null) codigos.add(c.toString().trim());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return codigos;
    }
}
