package com.panaderiafx.utils;

import java.util.*;

public class RelacionadorVisual {

    private static final String CONFIG_VISTA = "ConfiguraciónVistaTablas";

    public static List<Map<String, String>> aplicarSustituciones(String tablaActual, List<Map<String, String>> datos) {
        List<Map<String, String>> reglas = VerUtils.verTabla(CONFIG_VISTA);

        final String COL_TABLA_ORIGEN = "Tabla origen";
        final String COL_CAMPO_REEMPLAZAR = "Campo a reemplazar";
        final String COL_TABLA_REEMPLAZO = "Tabla de reemplazo";
        final String COL_CLAVE_REEMPLAZO = "Campo clave de reemplazo";
        final String COL_VALOR_VISIBLE = "Valor visible";

        List<Map<String, String>> reglasFiltradas = reglas.stream()
                .filter(r -> tablaActual.equalsIgnoreCase(r.getOrDefault(COL_TABLA_ORIGEN, "").trim()))
                .toList();

        if (reglasFiltradas.isEmpty()) return datos;

        // Precarga las tablas de referencia en caché
        Map<String, List<Map<String, String>>> cacheTablas = new HashMap<>();
        for (Map<String, String> regla : reglasFiltradas) {
            String tablaRef = regla.getOrDefault(COL_TABLA_REEMPLAZO, "").trim();
            cacheTablas.putIfAbsent(tablaRef, VerUtils.verTabla(tablaRef));
        }

        List<Map<String, String>> resultado = new ArrayList<>();

        for (Map<String, String> fila : datos) {
            Map<String, String> nuevaFila = new LinkedHashMap<>(fila);
        
            for (Map<String, String> regla : reglasFiltradas) {
                String campoReemplazar = regla.getOrDefault(COL_CAMPO_REEMPLAZAR, "").trim();
                String tablaReemplazo = regla.getOrDefault(COL_TABLA_REEMPLAZO, "").trim();
                String campoClave = regla.getOrDefault(COL_CLAVE_REEMPLAZO, "").trim();
                String campoVisible = regla.getOrDefault(COL_VALOR_VISIBLE, "").trim();
        
                String valorCodigoCrudo = fila.getOrDefault(campoReemplazar, "").trim();
                if (valorCodigoCrudo.isEmpty()) continue;
        
                List<Map<String, String>> refData = cacheTablas.get(tablaReemplazo);
                if (refData == null || refData.isEmpty()) continue;
        
                // ✅ Validación extra: largoClave no puede ser 0 ni fallar por dato vacío
                String primerClave = refData.get(0).getOrDefault(campoClave, "").trim();
                if (primerClave.isEmpty()) continue;
        
                int largoClave;
                try {
                    largoClave = primerClave.length();
                } catch (Exception e) {
                    System.err.println("⚠️ Error al obtener largo clave para: " + campoClave + " = '" + primerClave + "'");
                    continue;
                }
        
                String valorCodigo = String.format("%" + largoClave + "s", valorCodigoCrudo).replace(' ', '0');
        
                for (Map<String, String> ref : refData) {
                    String cod = ref.getOrDefault(campoClave, "").trim();
                    if (cod.equalsIgnoreCase(valorCodigo)) {
                        String reemplazo = ref.getOrDefault(campoVisible, valorCodigo);
                        if (!reemplazo.equals(valorCodigoCrudo)) {
                            nuevaFila.put(campoReemplazar, reemplazo);
                        }
                        break;
                    }
                }
            }
        
            resultado.add(nuevaFila);
        }
        
        return resultado;
    }
}
