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
        boolean okVariables = guardarVariableDia(); // Este ya incluye recalcular
        System.out.println("🟡 Resultado resumen: " + okResumen + " | variables: " + okVariables);
        return okResumen && okVariables;
    }

    private static boolean guardarResumen(LocalDate inicio, LocalDate fin) {
        try {
            System.out.println("🟠 Entrando a guardarResumen() con fechas: " + inicio + " → " + fin);
    
            List<Map<String, String>> resumen = VerUtils.verTabla(HOJA_RESUMEN);
            String codigo = generarCodigo(resumen);
            System.out.println("📌 Código generado para resumen: " + codigo);
    
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
            boolean todasExisten = true;
            for (String key : fila.keySet()) {
                if (!columnas.contains(key)) {
                    System.err.println("⚠️ COLUMNA NO ENCONTRADA EN EXCEL: " + key);
                    todasExisten = false;
                }
            }
    
            if (!todasExisten) {
                System.err.println("❌ Error: Faltan columnas en la hoja. No se intentará guardar.");
                return false;
            }
    
            // 🔍 Buscar si ya hay fila con el mismo rango de fechas
            Optional<Map<String, String>> existente = resumen.stream()
                .filter(r ->
                    inicio.format(FORMATO).equals(r.getOrDefault("Fecha Inicio", "")) &&
                    fin.format(FORMATO).equals(r.getOrDefault("Fecha Fin", ""))
                )
                .findFirst();
    
            if (existente.isPresent()) {
                String codigoExistente = existente.get().get("Código");
                System.out.println("✏️ Ya existe resumen con mismo rango. Reemplazando el código: " + codigoExistente);
                fila.put("Código", codigoExistente);
                boolean modificado = ModificarUtils.modificarFila(HOJA_RESUMEN,
                    Map.of("Código", codigoExistente),
                    fila);
                if (modificado) {
                    System.out.println("✅ Resumen semanal modificado correctamente en hoja '" + HOJA_RESUMEN + "'.");
                } else {
                    System.err.println("❌ Error al modificar el resumen existente.");
                }
                return modificado;
            }
    
            // Crear nuevo resumen si no existe uno con el mismo rango
            System.out.println("🟢 Intentando crear fila en hoja: " + HOJA_RESUMEN);
            fila.forEach((k, v) -> System.out.println("   ➤ [" + k + "] columna " + columnas.indexOf(k) + " ➝ " + v));
    
            boolean exito = CrearUtils.crearFila(HOJA_RESUMEN, fila);
            if (exito) {
                System.out.println("✅ Resumen semanal guardado correctamente en hoja '" + HOJA_RESUMEN + "'.");
            } else {
                System.err.println("❌ Error al guardar el resumen semanal (crearFila devolvió false).");
            }
    
            return exito;
    
        } catch (Exception e) {
            System.err.println("❌ Excepción al guardar resumen: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean guardarVariableDia() {
        try {
            System.out.println("🟠 Entrando a guardarVariableDia()");
            Map<String, Map<String, String>> cambios = EditorTemporalCache.obtenerTodo(HOJA_VARIABLE);
            if (cambios.isEmpty()) {
                System.out.println("📭 No hay cambios en VariableDia");
                return true;
            }

            List<Map<String, String>> actuales = VerUtils.verTabla(HOJA_VARIABLE);
            Set<String> existentes = new HashSet<>();
            for (Map<String, String> fila : actuales) {
                existentes.add(fila.getOrDefault("Código", ""));
            }

            List<Map<String, String>> filasFinales = new ArrayList<>();

            for (Map.Entry<String, Map<String, String>> entrada : cambios.entrySet()) {
                String codigo = entrada.getKey();
                Map<String, String> datos = entrada.getValue();

                System.out.println("📤 Guardando VariableDia → " + codigo);
                datos.forEach((k, v) -> System.out.printf("   ➤ %s = %s%n", k, v));

                boolean resultado;
                if (existentes.contains(codigo)) {
                    resultado = ModificarUtils.modificarFila(HOJA_VARIABLE, Map.of("Código", codigo), datos);
                    System.out.println("✏️ Modificado VariableDia: " + codigo);
                } else {
                    resultado = CrearUtils.crearFila(HOJA_VARIABLE, datos);
                    System.out.println("➕ Agregado VariableDia: " + codigo);
                }

                if (!resultado) {
                    System.err.println("❌ Falló guardado/modificación de VariableDia: " + codigo);
                    return false;
                }

                filasFinales.add(datos);
            }

            // 🔁 Recalcular el valor total de COSTOS_DIA y actualizar TOTAL
            recalcularCostosDiaYTotal(filasFinales);

            EditorTemporalCache.limpiarTabla(HOJA_VARIABLE);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Excepción al guardar VariableDia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void recalcularCostosDiaYTotal(List<Map<String, String>> filas) {
        double total = 0;
        for (Map<String, String> fila : filas) {
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
            String efecto = fila.getOrDefault("EFECTO", "-");
            if (efecto.equals("+")) total += valor;
            else total -= valor;
        }

        System.out.println("🔄 Recalculando COSTOS_DIA → " + total);
        CacheLibroSemanal.set(Tipo.COSTOS_DIA, total);

        double nuevoTotal =
            CacheLibroSemanal.get(Tipo.GANANCIA_B) -
            CacheLibroSemanal.get(Tipo.COSTO_DIRECTO) -
            CacheLibroSemanal.get(Tipo.COSTO_INDIRECTO) -
            CacheLibroSemanal.get(Tipo.PARAMETROS) -
            CacheLibroSemanal.get(Tipo.COSTOS_DIA);

        System.out.println("🧮 Recalculado TOTAL = " + nuevoTotal);
        CacheLibroSemanal.set(Tipo.TOTAL, nuevoTotal);
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
            System.err.println("⚠️ Error generando código, se usará valor por defecto.");
            return "RES" + String.format("%04d", new Random().nextInt(9999));
        }
    }
}
