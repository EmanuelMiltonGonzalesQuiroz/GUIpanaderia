package com.panaderiafx.utils.componentes;

import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.ModificarUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import com.panaderiafx.utils.cache.EditorTemporalCache;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GuardarLibroSemanalUtils {

    private static final String HOJA_RESUMEN = "ResumenSemanal";
    private static final String HOJA_VARIABLE = "VariableDia";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static boolean guardar(LocalDate inicio, LocalDate fin) {
        System.out.println("🟠 Entrando a guardar() → Inicio: " + inicio + ", Fin: " + fin);
        boolean okResumen = guardarResumen(inicio, fin);
        boolean okVariables = guardarVariableDia(); // ❌ ya no recalcula
        System.out.println("🟡 Resultado resumen: " + okResumen + " | variables: " + okVariables);
        return okResumen && okVariables;
    }

    private static boolean guardarResumen(LocalDate inicio, LocalDate fin) {
        try {
            List<Map<String, String>> resumen = VerUtils.verTabla(HOJA_RESUMEN);
            String codigo = generarCodigo(resumen);

            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("Código", codigo);
            fila.put("Fecha Inicio", inicio.format(FORMATO));
            fila.put("Fecha Fin", fin.format(FORMATO));
            fila.put("Ganancia B.", format(CacheLibroSemanal.get(Tipo.GANANCIA_B)));
            fila.put("Costos Directos", format(CacheLibroSemanal.get(Tipo.COSTO_DIRECTO)));
            fila.put("Costos Indirectos", format(CacheLibroSemanal.get(Tipo.COSTO_INDIRECTO)));
            fila.put("Parámetros", format(CacheLibroSemanal.get(Tipo.PARAMETROS)));
            fila.put("Variables Extras de Día", format(CacheLibroSemanal.get(Tipo.COSTOS_DIA)));
            fila.put("Total", format(CacheLibroSemanal.get(Tipo.TOTAL)));

            List<String> columnas = VerUtils.obtenerColumnas(HOJA_RESUMEN);
            boolean todasExisten = fila.keySet().stream().allMatch(columnas::contains);

            if (!todasExisten) {
                System.err.println("❌ Error: Faltan columnas en la hoja. No se guardará.");
                return false;
            }

            Optional<Map<String, String>> existente = resumen.stream()
                .filter(r -> inicio.format(FORMATO).equals(r.getOrDefault("Fecha Inicio", "")) &&
                             fin.format(FORMATO).equals(r.getOrDefault("Fecha Fin", "")))
                .findFirst();

            if (existente.isPresent()) {
                String codigoExistente = existente.get().get("Código");
                fila.put("Código", codigoExistente);
                return ModificarUtils.modificarFila(HOJA_RESUMEN, Map.of("Código", codigoExistente), fila);
            }

            return CrearUtils.crearFila(HOJA_RESUMEN, fila);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean guardarVariableDia() {
        try {
            Map<String, Map<String, String>> cambios = EditorTemporalCache.obtenerTodo(HOJA_VARIABLE);
            if (cambios.isEmpty()) return true;

            List<Map<String, String>> actuales = VerUtils.verTabla(HOJA_VARIABLE);
            Set<String> existentes = new HashSet<>();
            for (Map<String, String> fila : actuales) {
                existentes.add(fila.getOrDefault("Código", ""));
            }

            for (Map.Entry<String, Map<String, String>> entrada : cambios.entrySet()) {
                String codigo = entrada.getKey();
                Map<String, String> datos = entrada.getValue();

                boolean resultado;
                if (existentes.contains(codigo)) {
                    resultado = ModificarUtils.modificarFila(HOJA_VARIABLE, Map.of("Código", codigo), datos);
                } else {
                    resultado = CrearUtils.crearFila(HOJA_VARIABLE, datos);
                }

                if (!resultado) {
                    System.err.println("❌ Falló guardado de VariableDia: " + codigo);
                    return false;
                }
            }

            // 🧽 Limpiar cache temporal pero NO recalcular total
            EditorTemporalCache.limpiarTabla(HOJA_VARIABLE);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String format(double valor) {
        return String.format("%.2f", valor);
    }

    private static String generarCodigo(List<Map<String, String>> existentes) {
        try {
            int max = existentes.stream()
                    .map(m -> m.getOrDefault("Código", "RES0000"))
                    .filter(c -> c.matches("RES\\d{4}"))
                    .mapToInt(c -> Integer.parseInt(c.substring(3)))
                    .max().orElse(0);
            return "RES" + String.format("%04d", max + 1);
        } catch (Exception e) {
            return "RES" + String.format("%04d", new Random().nextInt(9999));
        }
    }
}
