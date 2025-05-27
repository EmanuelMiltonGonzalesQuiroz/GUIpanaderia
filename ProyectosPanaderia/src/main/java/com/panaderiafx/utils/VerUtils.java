package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class VerUtils {

    private static final String RUTA_EXCEL = "Datos\\Hoja de datos.xlsx";
    private static final Map<String, List<Map<String, String>>> cacheTablas = new HashMap<>();

    // ✅ Versión con caché (para uso general)
    public static List<Map<String, String>> verTablaConCache(String nombreTabla) {
        return verTabla(nombreTabla, true);
    }

    // ✅ Versión sin caché (para refrescar cambios hechos al Excel)
    public static List<Map<String, String>> verTabla(String nombreTabla) {
        return verTabla(nombreTabla, false);
    }

    // 🧠 Interno
    private static List<Map<String, String>> verTabla(String nombreTabla, boolean usarCache) {
        if (usarCache && cacheTablas.containsKey(nombreTabla)) {
            System.out.println("⚡ Recuperando tabla '" + nombreTabla + "' desde caché.");
            return cacheTablas.get(nombreTabla);
        }

        long inicio = System.currentTimeMillis();
        List<Map<String, String>> datos = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(RUTA_EXCEL);
             Workbook libro = WorkbookFactory.create(fis)) {

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
                    String[] encabezados = new String[colFin - colInicio + 1];
                    for (int c = colInicio; c <= colFin; c++) {
                        Cell celda = filaEncabezado.getCell(c);
                        encabezados[c - colInicio] = celda != null ? celda.toString().trim() : "";
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
                            filaMap.put(encabezados[c - colInicio], valor);
                        }

                        if (!vacia) datos.add(filaMap);
                    }

                    if (usarCache) cacheTablas.put(nombreTabla, datos);
                    System.out.println("📄 Cargada tabla '" + nombreTabla + "' desde rango nombrado. Filas: " + datos.size() +
                            " ⏱️ " + (System.currentTimeMillis() - inicio) + " ms");
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

                if (usarCache) cacheTablas.put(nombreTabla, datos);
                System.out.println("📄 Cargada tabla '" + nombreTabla + "' desde hoja. Filas: " + datos.size() +
                        " ⏱️ " + (System.currentTimeMillis() - inicio) + " ms");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al leer tabla u hoja '" + nombreTabla + "':");
            e.printStackTrace();
        }

        return datos;
    }

    // ✅ Forzar recarga de tabla específica
    public static void forzarActualizacion(String nombreTabla) {
        if (cacheTablas.remove(nombreTabla) != null) {
            System.out.println("♻️ Caché de tabla '" + nombreTabla + "' eliminada.");
        }
    }

    public static List<String> verColumna(String nombreTabla, String columna) {
        return verTabla(nombreTabla).stream()
                .map(fila -> fila.getOrDefault(columna, ""))
                .filter(s -> !s.isEmpty())
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

    public static List<String> obtenerColumnas(String nombreHoja) {
        List<String> columnas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(RUTA_EXCEL);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(nombreHoja);
            if (sheet == null) {
                System.err.println("❌ Hoja no encontrada: " + nombreHoja);
                return columnas;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                System.err.println("❌ Encabezado vacío en hoja: " + nombreHoja);
                return columnas;
            }

            for (int c = 0; c < header.getLastCellNum(); c++) {
                Cell celda = header.getCell(c);
                if (celda != null) {
                    columnas.add(celda.getStringCellValue().trim());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error al leer columnas de hoja " + nombreHoja + ": " + e.getMessage());
        }

        return columnas;
    }

    public static List<String> obtenerNombresTablas() {
        List<String> nombres = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(RUTA_EXCEL);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int total = workbook.getNumberOfSheets();
            for (int i = 0; i < total; i++) {
                nombres.add(workbook.getSheetName(i));
            }

        } catch (Exception e) {
            System.err.println("❌ Error al obtener nombres de hojas:");
            e.printStackTrace();
        }

        return nombres;
    }

    public static String buscarPorCodigo(String tabla, String campoClave, String valorClave, String campoRetorno) {
        return verTabla(tabla).stream()
                .filter(fila -> valorClave.equalsIgnoreCase(fila.getOrDefault(campoClave, "")))
                .map(fila -> fila.getOrDefault(campoRetorno, ""))
                .findFirst()
                .orElse("");
    }
}
