package com.panaderiafx.controllers.components.forms;

import com.panaderiafx.utils.VerUtils;


import java.util.*;

public class FormularioUtils {

    public static List<Map<String, Object>> generarInstrucciones(String tabla) {
        List<Map<String, Object>> definicion = new ArrayList<>();
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónFormularios");

        for (Map<String, String> fila : config) {
            if (!fila.getOrDefault("Tabla", "").equalsIgnoreCase(tabla)) continue;

            Map<String, Object> campo = new HashMap<>();
            campo.put("nombre", fila.get("Campo"));
            campo.put("tipo", fila.get("Tipo").toLowerCase());

            if ("select".equalsIgnoreCase(fila.get("Tipo"))) {
                campo.put("origen", fila.get("Origen"));
                campo.put("datoMostrar", fila.get("Dato a Mostrar"));
                campo.put("datoCargar", fila.get("Dato a Cargar"));
            }

            definicion.add(campo);
        }

        if (definicion.isEmpty()) {
            System.err.println("❌ No hay configuración definida para la tabla: " + tabla);
            return List.of(Map.of("nombre", "ERROR", "tipo", "label"));
        }

        return definicion;
    }
}
