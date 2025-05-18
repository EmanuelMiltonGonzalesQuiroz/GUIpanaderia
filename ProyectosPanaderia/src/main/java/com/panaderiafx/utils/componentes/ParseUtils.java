package com.panaderiafx.utils.componentes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ParseUtils {

    public static double toDouble(String texto) {
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static LocalDate toDate(String texto) {
        if (texto == null || texto.isEmpty()) return null;

        DateTimeFormatter[] formatos = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd") // por si viene con guiones
        };

        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(texto, formato);
            } catch (DateTimeParseException ignored) {}
        }

        return null; // No se pudo parsear
    }
}
