package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class VerUtils {

    private static final String RUTA_EXCEL = "Datos\\Hoja de datos.xlsx";

    public static List<Map<String, String>> verTabla(String nombreTabla) {
        List<Map<String, String>> datos = new ArrayList<>();

        try {
            // ⚠️ Forzar lectura "limpia" sin caché ni lock
            File tempFile = File.createTempFile("temp_excel", ".xlsx");
            Files.copy(new File(RUTA_EXCEL).toPath(), tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (InputStream fis = new FileInputStream(tempFile);
                 Workbook libro = new XSSFWorkbook(fis)) {

                Sheet sheet = null;

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
            }

            tempFile.deleteOnExit(); // se limpia al salir
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
    public static List<String> obtenerNombresTablas() {
        List<String> nombres = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(RUTA_EXCEL));
             Workbook workbook = WorkbookFactory.create(fis)) {

            int total = workbook.getNumberOfSheets();
            for (int i = 0; i < total; i++) {
                nombres.add(workbook.getSheetName(i));
            }

        } catch (Exception e) {
            System.err.println("❌ Error al obtener nombres de hojas: " + e.getMessage());
        }

        return nombres;
    }

}
