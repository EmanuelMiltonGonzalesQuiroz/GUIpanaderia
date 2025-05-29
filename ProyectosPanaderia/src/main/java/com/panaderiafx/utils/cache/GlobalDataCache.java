package com.panaderiafx.utils.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Cache global optimizado para evitar cargas repetitivas de tablas
 * Versión mejorada con mejor manejo de tipos genéricos
 */
public class GlobalDataCache {
    private static final Logger LOGGER = Logger.getLogger(GlobalDataCache.class.getName());
    private static final GlobalDataCache INSTANCE = new GlobalDataCache();
    
    // Cache thread-safe con locks para lectura/escritura
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Configuración del cache
    private static final long DEFAULT_TTL = 5 * 60 * 1000; // 5 minutos
    private static final long INGREDIENTES_TTL = 10 * 60 * 1000; // 10 minutos para ingredientes
    
    private GlobalDataCache() {}
    
    public static GlobalDataCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Obtiene datos del cache con validación de TTL
     */
    public <T> Optional<T> get(String tableName, Class<T> type) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(tableName);
            if (entry != null && !entry.isExpired()) {
                LOGGER.info("🚀 Cache HIT para tabla: " + tableName);
                return Optional.of(type.cast(entry.data));
            }
            LOGGER.info("❌ Cache MISS para tabla: " + tableName + 
                       (entry != null ? " (expirado)" : " (no existe)"));
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Métodos específicos para tipos comunes - evita problemas de generics
     */
    @SuppressWarnings("unchecked")
    public Optional<List<Map<String, String>>> getListOfMaps(String tableName) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(tableName);
            if (entry != null && !entry.isExpired()) {
                LOGGER.info("🚀 Cache HIT (ListOfMaps) para tabla: " + tableName);
                return Optional.of((List<Map<String, String>>) entry.data);
            }
            LOGGER.info("❌ Cache MISS (ListOfMaps) para tabla: " + tableName + 
                       (entry != null ? " (expirado)" : " (no existe)"));
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @SuppressWarnings("unchecked")
    public Optional<Map<String, String>> getStringMap(String tableName) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(tableName);
            if (entry != null && !entry.isExpired()) {
                LOGGER.info("🚀 Cache HIT (StringMap) para tabla: " + tableName);
                return Optional.of((Map<String, String>) entry.data);
            }
            LOGGER.info("❌ Cache MISS (StringMap) para tabla: " + tableName + 
                       (entry != null ? " (expirado)" : " (no existe)"));
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @SuppressWarnings("unchecked")
    public Optional<List<String>> getStringList(String tableName) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(tableName);
            if (entry != null && !entry.isExpired()) {
                LOGGER.info("🚀 Cache HIT (StringList) para tabla: " + tableName);
                return Optional.of((List<String>) entry.data);
            }
            LOGGER.info("❌ Cache MISS (StringList) para tabla: " + tableName + 
                       (entry != null ? " (expirado)" : " (no existe)"));
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Almacena datos en el cache con TTL personalizado
     */
    public <T> void put(String tableName, T data) {
        lock.writeLock().lock();
        try {
            long ttl = getCustomTTL(tableName);
            cache.put(tableName, new CacheEntry(data, System.currentTimeMillis() + ttl));
            LOGGER.info("💾 Datos cacheados para tabla: " + tableName + " (TTL: " + ttl + "ms)");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Invalida una tabla específica
     */
    public void invalidate(String tableName) {
        lock.writeLock().lock();
        try {
            cache.remove(tableName);
            LOGGER.info("🗑️ Cache invalidado para tabla: " + tableName);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Limpia todo el cache
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            LOGGER.info("🧹 Cache completamente limpiado");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Verifica si existe una entrada en cache (sin importar si está expirada)
     */
    public boolean exists(String tableName) {
        lock.readLock().lock();
        try {
            return cache.containsKey(tableName);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Verifica si existe una entrada válida (no expirada) en cache
     */
    public boolean isValid(String tableName) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cache.get(tableName);
            return entry != null && !entry.isExpired();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * TTL personalizado por tabla
     */
    private long getCustomTTL(String tableName) {
        String lowerName = tableName.toLowerCase();
        if (lowerName.contains("ingredientes") || lowerName.contains("recetas")) {
            return INGREDIENTES_TTL; // Datos más estables
        } else if (lowerName.contains("produccion")) {
            return DEFAULT_TTL / 2; // Datos más dinámicos
        } else {
            return DEFAULT_TTL;
        }
    }
    
    /**
     * Estadísticas del cache
     */
    public Map<String, Object> getStats() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEntries", cache.size());
            stats.put("expiredEntries", cache.values().stream().mapToLong(e -> e.isExpired() ? 1 : 0).sum());
            stats.put("validEntries", cache.values().stream().mapToLong(e -> !e.isExpired() ? 1 : 0).sum());
            
            // Detalles por tabla
            Map<String, String> tableStatus = new HashMap<>();
            cache.forEach((key, entry) -> {
                tableStatus.put(key, entry.isExpired() ? "EXPIRED" : "VALID");
            });
            stats.put("tableStatus", tableStatus);
            
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Limpia solo las entradas expiradas
     */
    public int cleanupExpired() {
        lock.writeLock().lock();
        try {
            int removed = 0;
            Iterator<Map.Entry<String, CacheEntry>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
                    removed++;
                    LOGGER.info("🗑️ Entrada expirada removida: " + entry.getKey());
                }
            }
            if (removed > 0) {
                LOGGER.info("🧹 " + removed + " entradas expiradas limpiadas del cache");
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private static class CacheEntry {
        final Object data;
        final long expiryTime;
        final long creationTime;
        
        CacheEntry(Object data, long expiryTime) {
            this.data = data;
            this.expiryTime = expiryTime;
            this.creationTime = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
        
        long getAge() {
            return System.currentTimeMillis() - creationTime;
        }
        
        long getTimeToExpiry() {
            return expiryTime - System.currentTimeMillis();
        }
    }
}