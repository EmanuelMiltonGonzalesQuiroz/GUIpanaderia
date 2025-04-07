package com.panaderiafx.utils;

import java.util.List;
import java.util.Map;

public class TablaViewer {
    public static void mostrarTabla(String tabla) {
        System.out.println("📘 Mostrando tabla " + tabla + "...");
        List<Map<String, String>> registros = ExcelDataUtils.obtenerTabla(tabla);
        for (Map<String, String> fila : registros) {
            System.out.println(fila);
        }
    }
}
