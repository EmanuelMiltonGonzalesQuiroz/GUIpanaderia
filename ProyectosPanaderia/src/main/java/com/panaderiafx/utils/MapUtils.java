package com.panaderiafx.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static Map<String, String> normalizarKeys(Map<String, String> original) {
        Map<String, String> resultado = new HashMap<>();
        for (Map.Entry<String, String> entry : original.entrySet()) {
            if (entry.getKey() != null) {
                resultado.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        return resultado;
    }
}
