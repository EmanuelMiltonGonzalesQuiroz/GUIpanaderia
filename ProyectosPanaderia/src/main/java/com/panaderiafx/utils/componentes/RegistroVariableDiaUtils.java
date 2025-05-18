package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.CodigoGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistroVariableDiaUtils {

    private static final String NOMBRE_HOJA = "VariableDia";

    public static void guardarCostoDelDia(String fecha, String tipo, String categoria, double costo) {
        try {
            if (fecha == null || tipo == null || categoria == null) {
                System.err.println("❌ Parámetros inválidos para guardar costo del día.");
                return;
            }

            String codigo = CodigoGenerator.generarCodigo(NOMBRE_HOJA, "Código");

            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("Código", codigo);
            fila.put("Fecha", fecha);
            fila.put("Tipo", tipo.toUpperCase().trim());
            fila.put("Categoría", categoria.trim());
            fila.put("Costo", String.format("%.2f", costo));

            CrearUtils.crearFila(NOMBRE_HOJA, fila);
            System.out.println("✅ Costo registrado en '" + NOMBRE_HOJA + "': " + fila);
        } catch (Exception e) {
            System.err.println("❌ Error al guardar en '" + NOMBRE_HOJA + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void guardarCostoDelDiaHoy(String tipo, String categoria, double costo) {
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        guardarCostoDelDia(fechaHoy, tipo, categoria, costo);
    }
}
