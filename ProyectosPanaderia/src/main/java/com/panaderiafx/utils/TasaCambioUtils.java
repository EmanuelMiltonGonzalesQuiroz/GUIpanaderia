package com.panaderiafx.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class TasaCambioUtils {

    public static double obtenerUltima() {
        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook wb = new XSSFWorkbook(file)) {

            XSSFSheet hoja = ExcelUtils.buscarHojaConTabla(wb, "TasaCambio");
            if (hoja == null) return 7.0;

            XSSFTable tabla = hoja.getTables().stream()
                    .filter(t -> t.getName().equalsIgnoreCase("TasaCambio"))
                    .findFirst().orElse(null);
            if (tabla == null) return 7.0;

            AreaReference area = new AreaReference(tabla.getArea().formatAsString(), wb.getSpreadsheetVersion());
            int filaInicio = area.getFirstCell().getRow();
            int filaFin = area.getLastCell().getRow();
            int colUSDaLocal = area.getFirstCell().getCol() + 1; // asumiendo: USD a Local = columna B

            for (int i = filaFin; i > filaInicio; i--) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
                Cell celda = fila.getCell(colUSDaLocal);
                if (celda != null && celda.getCellType() == CellType.NUMERIC) {
                    return celda.getNumericCellValue();
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 7.0;
    }
}
