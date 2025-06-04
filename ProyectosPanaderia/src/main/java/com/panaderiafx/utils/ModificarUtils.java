package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ModificarUtils {

    private static final String RUTA = "Datos\\Hoja de datos.xlsx";

    public static boolean modificarFila(String nombreTabla, Map<String, String> condiciones, Map<String, String> nuevosValores) {
        System.out.println("📋 Iniciando modificación de fila en tabla: " + nombreTabla);
        System.out.println("🔎 Condiciones: " + condiciones);
        System.out.println("🆕 Nuevos valores: " + nuevosValores);

        try (FileInputStream fis = new FileInputStream(RUTA);
             Workbook libro = new XSSFWorkbook(fis)) {

            Sheet hoja = libro.getSheet(nombreTabla);
            if (hoja == null) {
                System.err.println("❌ Hoja '" + nombreTabla + "' no encontrada.");
                return false;
            }

            Row headerRow = hoja.getRow(0);
            Map<String, Integer> columnas = new LinkedHashMap<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell celda = headerRow.getCell(c);
                if (celda != null) {
                    columnas.put(celda.getStringCellValue().trim(), c);
                }
            }
            System.out.println("📌 Columnas detectadas: " + columnas);

            for (int f = 1; f <= hoja.getLastRowNum(); f++) {
                Row fila = hoja.getRow(f);
                if (fila == null) continue;

                boolean coincide = condiciones.entrySet().stream()
                        .allMatch(e -> {
                            int col = columnas.getOrDefault(e.getKey(), -1);
                            if (col == -1) return false;
                            Cell celda = fila.getCell(col);
                            String contenido = celda != null ? celda.toString().trim() : "";
                            return e.getValue().equalsIgnoreCase(contenido);
                        });

                if (coincide) {
                    System.out.println("✅ Fila encontrada en la fila " + f + ". Aplicando cambios...");
                    for (Map.Entry<String, String> cambio : nuevosValores.entrySet()) {
                        if (columnas.containsKey(cambio.getKey())) {
                            int col = columnas.get(cambio.getKey());
                            Cell celda = fila.getCell(col);
                            if (celda == null) celda = fila.createCell(col);
                            celda.setCellValue(cambio.getValue());
                            System.out.println("✏️ [" + cambio.getKey() + "] actualizado a: " + cambio.getValue());
                        } else {
                            System.out.println("⚠️ Columna '" + cambio.getKey() + "' no encontrada.");
                        }
                    }

                    try (FileOutputStream fos = new FileOutputStream(RUTA)) {
                        libro.write(fos);
                        System.out.println("💾 Cambios guardados exitosamente.");
                    }

                    // ✅ Invalida caché para ver los cambios en memoria
                    System.out.println("Modificar");
                    VerUtils.refrescarExcel();
                    return true;
                }
            }

            System.err.println("❌ No se encontró una fila que coincida con las condiciones.");

        } catch (Exception e) {
            System.err.println("❌ Error al modificar fila: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static boolean modificarFilaSoloSiCambio(String nombreTabla, Map<String, String> condiciones, Map<String, String> nuevosValores) {
        Map<String, String> filaActual = VerUtils.verFila(nombreTabla, condiciones);
        if (filaActual == null) {
            System.err.println("❌ Fila no encontrada en '" + nombreTabla + "' para condiciones: " + condiciones);
            return false;
        }

        boolean hayCambios = nuevosValores.entrySet().stream().anyMatch(e ->
                !Objects.equals(filaActual.getOrDefault(e.getKey(), "").trim(), e.getValue().trim())
        );

        if (!hayCambios) {
            System.out.println("🔄 Sin cambios reales en fila de '" + nombreTabla + "', se omite escritura.");
            return false;
        }

        return modificarFila(nombreTabla, condiciones, nuevosValores);
    }
}