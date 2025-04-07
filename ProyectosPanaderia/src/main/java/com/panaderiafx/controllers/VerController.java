package com.panaderiafx.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class VerController {

    public static VBox mostrar(String tabla) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-spacing: 15; -fx-background-color: #FFF8E1;");

        Label titulo = new Label("Vista de registros de: " + tabla);
        titulo.setStyle("-fx-font-size: 18px;");

        ComboBox<Integer> selectorCantidad = new ComboBox<>();
        selectorCantidad.getItems().addAll(10, 20, 50, 100);
        selectorCantidad.setValue(20);
        selectorCantidad.setStyle("-fx-font-size: 14px;");

        HBox controles = new HBox(10, new Label("Mostrar:"), selectorCantidad);
        controles.setStyle("-fx-padding: 5; -fx-alignment: center-left;");

        Pagination paginador = new Pagination();
        paginador.setStyle("-fx-padding: 10;");

        TableView<Map<String, String>> tablaView = new TableView<>();
        tablaView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaView.setPrefHeight(400);

        // recargar siempre al abrir
        cargarDatosDesdeExcel(tabla, tablaView, paginador, selectorCantidad.getValue());

        // también recargar si cambia el selector
        selectorCantidad.setOnAction(e -> {
            cargarDatosDesdeExcel(tabla, tablaView, paginador, selectorCantidad.getValue());
        });

        layout.getChildren().addAll(titulo, controles, paginador);
        return layout;
    }

    private static void cargarDatosDesdeExcel(String tabla, TableView<Map<String, String>> tablaView, Pagination paginador, int cantidad) {
        ObservableList<Map<String, String>> datos = FXCollections.observableArrayList();
        List<String> columnas = new ArrayList<>();
        tablaView.getColumns().clear();

        try (InputStream file = new FileInputStream("C:\\Excel\\Datos\\Hoja de datos.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(file)) {

            XSSFSheet hoja = buscarHojaConTabla(workbook, tabla);
            if (hoja == null) return;

            XSSFTable excelTable = hoja.getTables().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(tabla))
                    .findFirst().orElse(null);
            if (excelTable == null) return;

            AreaReference area = new AreaReference(excelTable.getArea().formatAsString(), workbook.getSpreadsheetVersion());
            CellReference firstCell = area.getFirstCell();
            CellReference lastCell = area.getLastCell();

            int startRow = firstCell.getRow();
            int endRow = lastCell.getRow();
            int startCol = firstCell.getCol();
            int endCol = lastCell.getCol();

            Row encabezado = hoja.getRow(startRow);
            for (int c = startCol; c <= endCol; c++) {
                columnas.add(encabezado.getCell(c).getStringCellValue());
            }

            for (String col : columnas) {
                TableColumn<Map<String, String>, String> colView = new TableColumn<>(col);
                colView.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(col, "")));
                tablaView.getColumns().add(colView);
            }

            int vaciasConsecutivas = 0;
            for (int i = startRow + 1; i <= endRow; i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) {
                    vaciasConsecutivas++;
                    if (vaciasConsecutivas >= 20) break;
                    continue;
                }

                Map<String, String> filaData = new HashMap<>();
                boolean vacia = true;
                for (int j = startCol; j <= endCol; j++) {
                    Cell cell = fila.getCell(j);
                    String valor = obtenerValor(cell);
                    if (!valor.trim().isEmpty()) vacia = false;
                    filaData.put(columnas.get(j - startCol), valor);
                }

                if (vacia) {
                    vaciasConsecutivas++;
                    if (vaciasConsecutivas >= 20) break;
                    continue;
                }

                vaciasConsecutivas = 0;
                datos.add(filaData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        paginador.setPageCount((int) Math.ceil((double) datos.size() / cantidad));
        paginador.setCurrentPageIndex(0);
        paginador.setPageFactory(index -> {
            int inicio = index * cantidad;
            int fin = Math.min(inicio + cantidad, datos.size());
            tablaView.setItems(FXCollections.observableArrayList(datos.subList(inicio, fin)));
            return new VBox(tablaView);
        });
    }

    private static XSSFSheet buscarHojaConTabla(XSSFWorkbook wb, String nombreTabla) {
        for (Sheet sheet : wb) {
            if (sheet instanceof XSSFSheet xs) {
                for (XSSFTable t : xs.getTables()) {
                    if (t.getName().equalsIgnoreCase(nombreTabla)) {
                        return xs;
                    }
                }
            }
        }
        return null;
    }

    private static String obtenerValor(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : Double.toString(cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
