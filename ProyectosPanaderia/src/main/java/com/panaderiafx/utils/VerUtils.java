package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

import java.io.FileInputStream;
import java.util.*;

public class VerUtils {

    private static final String RUTA_EXCEL = "Datos\\Hoja de datos.xlsx";
    private static final Map<String, List<Map<String, String>>> cacheGlobal = new HashMap<>();

    // ✅ Cargar todo el Excel como JSON 
    public static void cargarTodoElExcel() {
        cacheGlobal.clear();
        long inicio = System.currentTimeMillis();
        try (FileInputStream fis = new FileInputStream(RUTA_EXCEL);
             Workbook libro = WorkbookFactory.create(fis)) {

            for (int i = 0; i < libro.getNumberOfSheets(); i++) {
                Sheet sheet = libro.getSheetAt(i);
                String nombreHoja = libro.getSheetName(i);
                List<Map<String, String>> filas = leerHoja(sheet);
                cacheGlobal.put(nombreHoja, filas);
                System.out.println("📄 Cargada hoja '" + nombreHoja + "'. Filas: " + filas.size());
            }

            for (Name namedRange : libro.getAllNames()) {
                String nombre = namedRange.getNameName();
                AreaReference area = new AreaReference(namedRange.getRefersToFormula(), libro.getSpreadsheetVersion());
                CellReference[] celdas = area.getAllReferencedCells();
                Sheet sheet = libro.getSheet(celdas[0].getSheetName());

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

                List<Map<String, String>> filas = new ArrayList<>();
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
                    if (!vacia) filas.add(filaMap);
                }

                cacheGlobal.put(nombre, filas);
                System.out.println("📌 Rango nombrado '" + nombre + "'. Filas: " + filas.size());
            }

            System.out.println("✅ Excel cargado completamente. Hojas totales: " + cacheGlobal.keySet().size()
                    + " ⏱️ Tiempo: " + (System.currentTimeMillis() - inicio) + " ms");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar Excel:");
            e.printStackTrace();
        }
    }

    // 🔄 Refrescar (recargar) el Excel
    public static void refrescarExcel() {
        System.out.println("♻️ Refrescando datos desde archivo Excel...");
        cargarTodoElExcel();
    }

    // 📄 Obtener tabla desde cache
    public static List<Map<String, String>> verTabla(String nombreTabla) {
        return cacheGlobal.getOrDefault(nombreTabla, new ArrayList<>());
    }

    // 🧠 Ver una fila por filtros
    public static Map<String, String> verFila(String nombreTabla, Map<String, String> filtros) {
        return verTabla(nombreTabla).stream()
                .filter(fila -> filtros.entrySet().stream()
                        .allMatch(f -> f.getValue().equalsIgnoreCase(fila.getOrDefault(f.getKey(), ""))))
                .findFirst()
                .orElse(null);
    }

    // 📋 Ver varias filas por filtros
    public static List<Map<String, String>> verFilas(String nombreTabla, Map<String, String> filtros) {
        return verTabla(nombreTabla).stream()
                .filter(fila -> filtros.entrySet().stream()
                        .allMatch(f -> f.getValue().equalsIgnoreCase(fila.getOrDefault(f.getKey(), ""))))
                .toList();
    }

    public static String buscarPorCodigo(String tabla, String campoClave, String valorClave, String campoRetorno) {
        if (valorClave == null || valorClave.isBlank()) return "";

        return verTabla(tabla).stream()
                .filter(fila -> valorClave.equalsIgnoreCase(fila.getOrDefault(campoClave, "")))
                .map(fila -> fila.getOrDefault(campoRetorno, ""))
                .findFirst()
                .orElse("");
    }


    // 📑 Ver una columna específica
    public static List<String> verColumna(String nombreTabla, String columna) {
        return verTabla(nombreTabla).stream()
                .map(fila -> fila.getOrDefault(columna, ""))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // 📦 Ver nombres de tablas cargadas
    public static List<String> obtenerNombresTablas() {
        return new ArrayList<>(cacheGlobal.keySet());
    }

    // 📊 Ver columnas de una tabla
    public static List<String> obtenerColumnas(String nombreTabla) {
        List<Map<String, String>> tabla = verTabla(nombreTabla);
        if (!tabla.isEmpty()) {
            return new ArrayList<>(tabla.get(0).keySet());
        }
        return new ArrayList<>();
    }

    // 📥 Interno - leer hoja sin rangos
    private static List<Map<String, String>> leerHoja(Sheet sheet) {
        List<Map<String, String>> datos = new ArrayList<>();
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

        return datos;
    }
}
