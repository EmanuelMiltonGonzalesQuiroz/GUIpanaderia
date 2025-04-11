package com.panaderiafx.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TasaCambioUtils {

    public static double obtenerUltimaTasa() {
        List<Map<String, String>> datos = VerUtils.verTabla("TasaCambio");

        if (datos == null || datos.isEmpty()) {
            System.err.println("⚠️ No se encontraron datos en la tabla 'TasaCambio'.");
            return 1.0;
        }

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate ultimaFecha = null;
        double ultimaTasa = 1.0;

        for (Map<String, String> fila : datos) {
            try {
                String fechaTexto = fila.get("Fecha");
                String tasaTexto = fila.get("Local a USD");
                if (fechaTexto == null || tasaTexto == null) continue;

                LocalDate fecha = LocalDate.parse(fechaTexto, formato);
                double tasa = Double.parseDouble(tasaTexto);

                if (ultimaFecha == null || fecha.isAfter(ultimaFecha)) {
                    ultimaFecha = fecha;
                    ultimaTasa = tasa;
                }
            } catch (Exception e) {
                System.err.println("❌ Error procesando fila: " + e.getMessage());
            }
        }

        System.out.println("✅ Última tasa encontrada (" + ultimaFecha + ") ➤ " + ultimaTasa);
        return ultimaTasa;
    }
}
