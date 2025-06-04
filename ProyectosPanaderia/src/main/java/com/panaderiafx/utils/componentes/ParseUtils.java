package com.panaderiafx.utils.componentes;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

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

    public static double safeParseDouble(String val) {
        if (val == null) return 0;
        try {
            // Primero intenta convertir con punto
            return Double.parseDouble(val.trim().replace(",", "."));
        } catch (NumberFormatException e1) {
            try {
                // Si falla, usa un parser más tolerante con coma
                NumberFormat format = NumberFormat.getInstance(Locale.US);
                return format.parse(val.trim().replace(",", ".")).doubleValue();
            } catch (ParseException e2) {
                return 0;
            }
        }
    }

    public static boolean esNumero(String val) {
        try {
            Double.parseDouble(val.trim().replace(",", "."));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static double parseDouble(String val) {
        return safeParseDouble(val);
    }

}
