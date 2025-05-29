package com.panaderiafx.utils;

import com.panaderiafx.utils.cache.GlobalDataCache;
import java.util.*;
import java.util.logging.Logger;

/**
 * Wrapper para VerUtils que intercepta llamadas y usa cache global
 */
public class VerUtilsOptimized {
    
    private static final Logger LOGGER = Logger.getLogger(VerUtilsOptimized.class.getName());
    private static final GlobalDataCache cache = GlobalDataCache.getInstance();
    
    // Tablas que nunca deben cachearse (siempre frescas)
    private static final Set<String> TABLAS_NO_CACHE = Set.of(
        "Logs", "TempData", "SessionData"
    );
    
    /**
     * Versión optimizada de verTabla que usa cache global
     */
    public static List<Map<String, String>> verTabla(String nombreTabla) {
        // Verificar si la tabla no debe cachearse
        if (TABLAS_NO_CACHE.contains(nombreTabla)) {
            LOGGER.info("🚫 Tabla " + nombreTabla + " marcada como no-cache, cargando directamente");
            return VerUtils.verTabla(nombreTabla);
        }
        
        // Intentar obtener desde cache primero
        Optional<List<Map<String, String>>> cached = cache.getListOfMaps(nombreTabla);
        if (cached.isPresent()) {
            LOGGER.info("🚀 VerUtils CACHE HIT para: " + nombreTabla);
            return cached.get();
        }
        
        // Si no está en cache, cargar y cachear
        LOGGER.info("📥 VerUtils cargando tabla: " + nombreTabla);
        long startTime = System.currentTimeMillis();
        
        List<Map<String, String>> data = VerUtils.verTabla(nombreTabla);
        
        long loadTime = System.currentTimeMillis() - startTime;
        LOGGER.info(String.format("📄 VerUtils cargó %s: %d filas en %d ms", 
                                nombreTabla, data.size(), loadTime));
        
        // Cachear el resultado
        cache.put(nombreTabla, data);
        
        return data;
    }
    
    /**
     * Versión optimizada de verTablaConCache
     */
    public static List<Map<String, String>> verTablaConCache(String nombreTabla) {
        return verTabla(nombreTabla); // Usa la versión optimizada
    }
    
    /**
     * Versión optimizada de verFilas con cache
     */
    public static List<Map<String, String>> verFilas(String nombreTabla, Map<String, String> filtros) {
        String cacheKey = nombreTabla + "_FILTRO_" + filtros.hashCode();
        
        Optional<List<Map<String, String>>> cached = cache.getListOfMaps(cacheKey);
        if (cached.isPresent()) {
            LOGGER.info("🚀 VerUtils CACHE HIT (filtrado) para: " + cacheKey);
            return cached.get();
        }
        
        LOGGER.info("📥 VerUtils cargando con filtros: " + nombreTabla);
        List<Map<String, String>> data = VerUtils.verFilas(nombreTabla, filtros);
        
        // Cachear con TTL más corto para datos filtrados
        cache.put(cacheKey, data);
        
        return data;
    }
    
    /**
     * Invalida cache cuando se actualiza una tabla
     */
    public static void forzarActualizacion(String nombreTabla) {
        cache.invalidate(nombreTabla);
        // También invalidar filtros relacionados
        cache.getStats().entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(nombreTabla + "_FILTRO_"))
                .forEach(entry -> cache.invalidate(entry.getKey().toString()));
        
        VerUtils.forzarActualizacion(nombreTabla);
        LOGGER.info("🔄 Forzada actualización y cache invalidado para: " + nombreTabla);
    }
    
    /**
     * Delegar otros métodos directamente a VerUtils
     */
    public static String buscarPorCodigo(String tabla, String columnaCodigo, String codigo, String columnaResultado) {
        return VerUtils.buscarPorCodigo(tabla, columnaCodigo, codigo, columnaResultado);
    }
    
    public static Map<String, String> verFila(String tabla, Map<String, String> filtros) {
        return VerUtils.verFila(tabla, filtros);
    }
}