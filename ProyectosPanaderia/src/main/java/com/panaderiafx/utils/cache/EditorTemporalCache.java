package com.panaderiafx.utils.cache;

import java.util.HashMap;
import java.util.Map;

public class EditorTemporalCache {

    private static final Map<String, Map<String, Map<String, String>>> cache = new HashMap<>();

    // 🔹 Guardar o actualizar una fila modificada
    public static void guardarFila(String tabla, String clave, Map<String, String> fila) {
        cache.computeIfAbsent(tabla, k -> new HashMap<>()).put(clave, new HashMap<>(fila));
        System.out.printf("📝 Fila guardada en cache [%s][%s]: %s\n", tabla, clave, fila);
    }

    // 🔹 Obtener fila editada (si existe)
    public static Map<String, String> obtenerFila(String tabla, String clave) {
        return cache.getOrDefault(tabla, new HashMap<>()).get(clave);
    }

    // 🔹 Aplicar los datos cacheados sobre la fuente original
    public static void aplicarCache(String tabla, String columnaClave, java.util.List<Map<String, String>> fuente) {
        Map<String, Map<String, String>> cambios = cache.getOrDefault(tabla, new HashMap<>());
        for (Map<String, String> fila : fuente) {
            String clave = fila.getOrDefault(columnaClave, "");
            if (cambios.containsKey(clave)) {
                fila.putAll(cambios.get(clave));
                System.out.printf("✅ Cache aplicado sobre fila [%s] en %s\n", clave, tabla);
            }
        }
    }

    // 🔹 Obtener todas las filas modificadas de una tabla
    public static Map<String, Map<String, String>> obtenerTodo(String tabla) {
        return cache.getOrDefault(tabla, new HashMap<>());
    }

    // 🔹 Eliminar todos los cambios de una tabla
    public static void limpiarTabla(String tabla) {
        cache.remove(tabla);
    }

    // ✅ Alias público para limpiarTabla()
    public static void limpiar(String tabla) {
        limpiarTabla(tabla);
    }

    // 🔹 Verificar si existe algo modificado en una tabla
    public static boolean tieneCambios(String tabla) {
        return cache.containsKey(tabla) && !cache.get(tabla).isEmpty();
    }

    // 🔹 Borrar todo el cache
    public static void limpiarTodo() {
        cache.clear();
    }
    // ✅ Alias de obtenerTodo
    public static Map<String, Map<String, String>> getCambios(String tabla) {
        return obtenerTodo(tabla);
    }
    // ✅ Reemplazar todos los cambios de una tabla con un nuevo mapa

    public static void setCambios(String tabla, Map<String, Map<String, String>> nuevos) {
        cache.put(tabla, nuevos);
        System.out.printf("🔁 Cambios reemplazados en cache [%s] con %d registros\n", tabla, nuevos.size());
    }
    

}
