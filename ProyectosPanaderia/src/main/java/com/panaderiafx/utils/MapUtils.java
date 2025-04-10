package com.panaderiafx.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static Map<String, String> normalizarKeys(Map<String, String> original) {
        Map<String, String> resultado = new HashMap<>();

        for (Map.Entry<String, String> entry : original.entrySet()) {
            String clave = entry.getKey()
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replace("≤", "o") // ← arregla caracteres corruptos
                .replace("≥", "a")
                .replace("–", "-")
                .replace("´", "")
                .replace("’", "")
                .replace("‘", "")
                .replace("“", "")
                .replace("”", "")
                .replace("‘", "")
                .replace("’", "")
                .replace(" ", "_") // opcional: convierte espacios en _
                .trim();

            resultado.put(clave, entry.getValue());
        }

        return resultado;
    }
}
