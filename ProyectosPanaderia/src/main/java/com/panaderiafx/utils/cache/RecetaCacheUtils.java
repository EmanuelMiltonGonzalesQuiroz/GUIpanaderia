package com.panaderiafx.utils.cache;

import com.panaderiafx.utils.VerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecetaCacheUtils {

    private static final Map<String, String> mapaCodigoANombre = new HashMap<>();

    public static void cargar() {
        mapaCodigoANombre.clear();
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        for (Map<String, String> fila : recetas) {
            String codigo = fila.getOrDefault("Código receta", "").trim();
            String nombre = fila.getOrDefault("Producto", "").trim();
            if (!codigo.isEmpty()) {
                mapaCodigoANombre.put(codigo, nombre);
            }
        }
    }

    public static String obtenerNombre(String codigo) {
        return mapaCodigoANombre.getOrDefault(codigo, codigo);
    }
}
