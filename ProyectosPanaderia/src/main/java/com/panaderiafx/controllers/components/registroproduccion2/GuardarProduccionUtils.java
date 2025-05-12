package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.CodigoGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GuardarProduccionUtils {

    public static void guardar(String codReceta, String fecha, String cantidad, String precioUnitario, String totalCalculado) {
        Map<String, String> fila = new LinkedHashMap<>();

        // Convertir fecha si está en formato yyyy-MM-dd
        fecha = convertirFechaSiEsNecesario(fecha);

        // Generar código automáticamente
        String nuevoCodigo = CodigoGenerator.generarCodigo("Produccion", "Código Producción");

        fila.put("Código Producción", nuevoCodigo);
        fila.put("Fecha", fecha);
        fila.put("Código receta", codReceta);
        fila.put("Cantidad producida", cantidad);
        fila.put("Precio de Venta por Unidad", precioUnitario);

        CrearUtils.crearFila("Produccion", fila);
        System.out.println("✅ Producción registrada: " + fila);
    }

    private static String convertirFechaSiEsNecesario(String fecha) {
        try {
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat salida = new SimpleDateFormat("dd/MM/yyyy");
                return salida.format(entrada.parse(fecha));
            }
        } catch (ParseException e) {
            System.err.println("⚠️ Error al convertir la fecha: " + fecha);
        }
        return fecha; // Retorna la original si ya está bien o no se pudo convertir
    }
}
