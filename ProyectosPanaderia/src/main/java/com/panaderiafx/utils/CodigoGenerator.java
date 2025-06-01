package com.panaderiafx.utils;

import com.panaderiafx.utils.cache.EditorTemporalCache;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodigoGenerator {

    public static String generarCodigo(String nombreTabla, String columnaCodigo) {
        System.out.println("🔵 Iniciando generación de código para tabla: " + nombreTabla);
        List<Map<String, String>> registros = VerUtils.verTabla(nombreTabla);

        if (registros.isEmpty()) {
            System.out.println("❌ No hay registros en la tabla: " + nombreTabla);
            return "ERR0000";
        }

        Set<String> columnas = registros.get(0).keySet();
        System.out.println("🔎 Columnas encontradas: " + columnas);

        // Si no se pasa columna, o si la columna pasada NO existe, buscar la mejor opción
        if (columnaCodigo == null || columnaCodigo.isBlank() || !columnas.contains(columnaCodigo)) {
            System.out.println("⚠️ Columna proporcionada no válida: " + columnaCodigo);

            // Buscar primero alguna columna que contenga "codigo" o "codigoreceta"
            for (String col : columnas) {
                String normalizado = Normalizer.normalize(col, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "") // Elimina tildes
                        .toLowerCase()
                        .replaceAll("[^a-z]", ""); // Elimina espacios y caracteres no alfabéticos

                System.out.println("🔍 Evaluando columna original: '" + col + "' -> normalizado: '" + normalizado + "'");

                if (normalizado.contains("codigoreceta") || normalizado.contains("codigo")) {
                    columnaCodigo = col;
                    System.out.println("✅ Columna de código detectada por contenido: " + columnaCodigo);
                    break;
                }
            }

            // Si tampoco encuentra por contenido, usar la primera columna
            if (columnaCodigo == null || columnaCodigo.isBlank() || !columnas.contains(columnaCodigo)) {
                columnaCodigo = columnas.iterator().next();
                System.out.println("✅ Usando primera columna disponible: " + columnaCodigo);
            }
        } else {
            System.out.println("🟡 Columna de código proporcionada: " + columnaCodigo);
        }

        List<String> codigosCrudos = new ArrayList<>();
        
        // ✅ AGREGAR CÓDIGOS DEL EXCEL
        for (Map<String, String> fila : registros) {
            String valor = fila.getOrDefault(columnaCodigo, "").trim();
            if (!valor.isBlank()) {
                codigosCrudos.add(valor);
            }
        }
        
        // ✅ TAMBIÉN AGREGAR CÓDIGOS DEL CACHE TEMPORAL
        Map<String, Map<String, String>> cacheTabla = EditorTemporalCache.getCambios(nombreTabla);
        if (cacheTabla != null) {
            for (Map<String, String> filaCache : cacheTabla.values()) {
                String codigoCache = filaCache.getOrDefault(columnaCodigo, "").trim();
                if (!codigoCache.isBlank()) {
                    codigosCrudos.add(codigoCache);
                    System.out.println("📦 Código encontrado en cache: " + codigoCache);
                }
            }
        }
        
        System.out.println("🗂️ Códigos totales extraídos (Excel + Cache): " + codigosCrudos);

        // Corregido: Ahora acepta 3 o 4 letras en el prefijo
        Pattern pattern = Pattern.compile("^([A-Z]{3,4})(\\d{3,4})$");
        Map<String, Set<Integer>> codigosPorPrefijo = new LinkedHashMap<>();

        for (String codigo : codigosCrudos) {
            Matcher matcher = pattern.matcher(codigo.toUpperCase());
            if (matcher.matches()) {
                String prefijo = matcher.group(1);
                int numero = Integer.parseInt(matcher.group(2));

                codigosPorPrefijo.putIfAbsent(prefijo, new TreeSet<>());
                codigosPorPrefijo.get(prefijo).add(numero);
                
                System.out.printf("✅ Código válido: %s → Prefijo: %s, Número: %d%n", codigo, prefijo, numero);
            } else {
                System.out.println("⚠️ Código no válido ignorado: " + codigo);
            }
        }

        if (codigosPorPrefijo.isEmpty()) {
            System.out.println("❌ No se encontró ningún código válido en la tabla.");
            return "ERR0000";
        }

        // Usar el primer prefijo encontrado
        String prefijo = codigosPorPrefijo.keySet().iterator().next();
        Set<Integer> usados = codigosPorPrefijo.get(prefijo);

        System.out.printf("🔧 Usando prefijo: %s, números usados: %s%n", prefijo, usados);

        for (int i = 1; i <= 9999; i++) {
            if (!usados.contains(i)) {
                String nuevoCodigo = prefijo + String.format("%04d", i);
                System.out.println("✅ Código generado: " + nuevoCodigo);
                return nuevoCodigo;
            }
        }

        System.out.println("⚠️ Se usaron todos los códigos posibles.");
        return prefijo + "9999";
    }
}