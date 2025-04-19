package com.panaderiafx.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RegistroCambiosUtils {

    public static void validarYCrearRegistro(String tabla, Map<String, String> condiciones, Map<String, String> nuevosValores) {
        System.out.println("🔎 Verificando cambios para la tabla: " + tabla);

        try {
            if ("Ingredientes".equalsIgnoreCase(tabla)) {
                manejarCambioPrecioIngredientes(condiciones, nuevosValores);
                manejarCambioUnidadRecetasIngredientes(condiciones, nuevosValores);
            } else if ("Recetas".equalsIgnoreCase(tabla)) {
                manejarVersionReceta(condiciones, nuevosValores);
            }
        } catch (Exception e) {
            System.err.println("❌ Error general en validarYCrearRegistro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void manejarCambioPrecioIngredientes(Map<String, String> condiciones, Map<String, String> nuevosValores) {
        String codigo = obtenerValorSeguro("Código", condiciones, nuevosValores);
        String nombreAntes = obtenerValorSeguro("Nombre", condiciones, nuevosValores);
        String precioLocalAntes = obtenerValorSeguro("Precio Local", condiciones, nuevosValores, "0");
        String precioDolarAntes = obtenerValorSeguro("Precio Dólar", condiciones, nuevosValores, "0");
    
        String precioLocalNuevo = nuevosValores.get("Precio Local");
        String precioDolarNuevo = nuevosValores.get("Precio Dólar");
    
        boolean cambioLocal = precioLocalNuevo != null &&
                Math.abs(Double.parseDouble(precioLocalNuevo) - Double.parseDouble(precioLocalAntes)) > 0.0001;
    
        boolean cambioDolar = precioDolarNuevo != null &&
                Math.abs(Double.parseDouble(precioDolarNuevo) - Double.parseDouble(precioDolarAntes)) > 0.0001;
    
        List<Map<String, String>> historialAntes = VerUtils.verTabla("HistorialPrecios");
        System.out.println("📋 HistorialPrecios ANTES:");
        historialAntes.forEach(System.out::println);
    
        boolean creandoNuevo = condiciones == null || condiciones.isEmpty();

        if (creandoNuevo || cambioLocal || cambioDolar) {
            System.out.println("📌 Cambio detectado ➤ " +
                    (creandoNuevo ? "[Nuevo Registro] " : "") +
                    (cambioLocal ? "[Precio Local] " : "") +
                    (cambioDolar ? "[Precio Dólar] " : ""));

            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("Código", codigo);
            fila.put("Item", nombreAntes);
            fila.put("Fecha", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            fila.put("Precio Local", precioLocalNuevo != null ? precioLocalNuevo : precioLocalAntes);
            fila.put("Precio Dólar", precioDolarNuevo != null ? precioDolarNuevo : precioDolarAntes);

            System.out.println("📝 Registrando fila: " + fila);
            CrearUtils.crearFila("HistorialPrecios", fila);
        }
        else {
            System.out.println("🟡 No hubo cambios significativos de precios.");
        }
    }
    
    private static void manejarCambioUnidadRecetasIngredientes(Map<String, String> condiciones, Map<String, String> nuevosValores) {
        String codigoIngrediente = condiciones.get("Código");
        String unidadAntes = condiciones.get("Unidad");
        String unidadNueva = nuevosValores.get("Unidad");

        if (unidadAntes != null && unidadNueva != null && !unidadAntes.equalsIgnoreCase(unidadNueva)) {
            List<Map<String, String>> recetas = VerUtils.verTabla("RecetasIngredientes");

            System.out.println("📋 RecetasIngredientes ANTES (posibles afectados):");
            recetas.stream()
                    .filter(receta -> receta.get("Ingrediente").equalsIgnoreCase(codigoIngrediente))
                    .forEach(System.out::println);

            for (Map<String, String> receta : recetas) {
                if (receta.get("Ingrediente").equalsIgnoreCase(codigoIngrediente)) {
                    Map<String, String> filaNueva = new LinkedHashMap<>(receta);
                    filaNueva.put("Unidades", unidadNueva);

                    System.out.println("🔄 Actualizando unidad ➝ " +
                            receta.get("Código Receta") + " [" +
                            unidadAntes + " → " + unidadNueva + "]");

                    ModificarUtils.modificarFila("RecetasIngredientes", receta, filaNueva);
                }
            }

            List<Map<String, String>> recetasDespues = VerUtils.verTabla("RecetasIngredientes");
            System.out.println("📋 RecetasIngredientes DESPUÉS:");
            recetasDespues.stream()
                    .filter(receta -> receta.get("Ingrediente").equalsIgnoreCase(codigoIngrediente))
                    .forEach(System.out::println);
        } else {
            System.out.println("🟡 No hubo cambio de unidad en ingredientes.");
        }
    }

    private static void manejarVersionReceta(Map<String, String> condiciones, Map<String, String> nuevosValores) {
        String receta = condiciones.getOrDefault("Producto", "");
        String rendimiento = nuevosValores.get("Rendimiento esperado");
        String ganancia = nuevosValores.get("Ganancia esperada");

        if (rendimiento != null || ganancia != null) {
            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("CódigoVersionReceta", "Auto");
            fila.put("Receta", receta);
            fila.put("Version", String.valueOf(System.currentTimeMillis()));
            fila.put("Rendimiento esperado en unidades", rendimiento != null ? rendimiento : "0");
            fila.put("Porcentaje de ganancia esperado", ganancia != null ? ganancia : "0");

            System.out.println("📝 Creando nueva versión en RecetasVersion: " + fila);
            CrearUtils.crearFila("RecetasVersion", fila);
        } else {
            System.out.println("🟡 No se detectó cambio en rendimiento o ganancia.");
        }
    }
    private static String obtenerValorSeguro(String clave, Map<String, String> condiciones, Map<String, String> nuevos, String valorDefecto) {
        if (condiciones != null && condiciones.containsKey(clave) && !condiciones.get(clave).isBlank()) {
            return condiciones.get(clave);
        }
        if (nuevos != null && nuevos.containsKey(clave) && !nuevos.get(clave).isBlank()) {
            return nuevos.get(clave);
        }
        return valorDefecto;
    }
    
    
    private static String obtenerValorSeguro(String clave, Map<String, String> condiciones, Map<String, String> nuevos) {
        return obtenerValorSeguro(clave, condiciones, nuevos, "");
    }
    
}
