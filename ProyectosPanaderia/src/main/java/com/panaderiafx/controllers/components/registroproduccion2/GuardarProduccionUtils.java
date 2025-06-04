package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.controllers.components.registroproduccion2.receta.PanelIngredientesRecetaConMezclas;
import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.control.Alert;

import java.text.SimpleDateFormat;
import java.util.*;

public class GuardarProduccionUtils {

    public static void guardar(Map<String, String> fila, boolean guardarReceta) {
        System.out.println("🚀 Iniciando guardado de producción...");
        
        VerUtils.refrescarExcel();  // 🔄 Refrescar antes de leer o escribir

        String fechaOriginal = fila.get("Fecha");
        String fechaFormateada = convertirFechaSiEsNecesario(fechaOriginal);
        fila.put("Fecha", fechaFormateada);

        String[] codRecetaFinal = { fila.getOrDefault("Código receta", "") };

        if (guardarReceta) {
            String codRecetaAnterior = fila.get("Código receta");
            String nombreReceta = fila.getOrDefault("Producto", "-");

            codRecetaFinal[0] = CodigoGenerator.generarCodigo("Recetas", "Código receta");
            String nuevaVersion = calcularSiguienteVersionPorProducto(nombreReceta);

            List<Map<String, String>> ingredientes = PanelIngredientesRecetaConMezclas.obtenerIngredientesModificados();

            for (Map<String, String> ing : ingredientes) {
                if (!"✓".equals(ing.get("Check"))) continue;

                Map<String, String> nueva = new LinkedHashMap<>();
                nueva.put("Código receta", codRecetaFinal[0]);
                nueva.put("Receta", nombreReceta);
                nueva.put("Ingrediente", ing.getOrDefault("Ingrediente", ""));
                nueva.put("Cantidad", ing.getOrDefault("Cantidad", ""));
                nueva.put("Unidades", ing.getOrDefault("Unidades", ""));
                nueva.put("Versión", nuevaVersion);
                CrearUtils.crearFila("RecetasIngredientes", nueva);
            }

            Optional<Map<String, String>> base = VerUtils.verTabla("Recetas").stream()
                    .filter(r -> codRecetaAnterior.equalsIgnoreCase(r.get("Código receta")))
                    .findFirst();

            base.ifPresent(r -> {
                Map<String, String> nueva = new LinkedHashMap<>(r);
                nueva.put("Código receta", codRecetaFinal[0]);
                nueva.put("Rendimiento", fila.getOrDefault("Cantidad producida", ""));
                nueva.put("Versión", nuevaVersion);
                CrearUtils.crearFila("Recetas", nueva);
            });

            mostrarConfirmacion("✅ Producción y receta " + codRecetaFinal[0] + " guardadas.");
        }

        String codigoProduccion = CodigoGenerator.generarCodigo("Produccion", "Código Producción");

        // 🔧 CALCULAR PRECIO POR UNIDAD CORREGIDO
        double cantidadProducida = ParseUtils.safeParseDouble(fila.getOrDefault("Cantidad producida", "0"));
        double precioRegistrado = ParseUtils.safeParseDouble(fila.getOrDefault("Precio registrado", "0"));
        
        System.out.println("💰 Calculando precio por unidad:");
        System.out.println("   Precio registrado: " + precioRegistrado);
        System.out.println("   Cantidad producida: " + cantidadProducida);
        
        // Obtener datos para verificar si es precio por molde
        String unidadesStr = fila.getOrDefault("Unidades por Molde", "").trim();
        String moldesStr = fila.getOrDefault("Molde/Paquete", "").trim();
        
        // 🔧 CORRECCIÓN: Verificar si tenemos precio por unidad ya calculado
        String precioUnidadExistente = fila.getOrDefault("Precio de Venta por Unidad", "").trim();
        double precioUnidad;
        
        if (!precioUnidadExistente.isEmpty() && ParseUtils.esNumero(precioUnidadExistente)) {
            // Si ya tenemos el precio por unidad calculado en el formulario, usarlo
            precioUnidad = ParseUtils.safeParseDouble(precioUnidadExistente);
            System.out.println("   Usando precio unitario del formulario: " + precioUnidad);
        } else {
            // Calcular desde precio registrado
            boolean esPrecioPorMolde = ParseUtils.esNumero(unidadesStr) && ParseUtils.esNumero(moldesStr);
            precioUnidad = esPrecioPorMolde ? 
                precioRegistrado / ParseUtils.safeParseDouble(unidadesStr) : 
                precioRegistrado;
                
            System.out.println("   Es precio por molde: " + esPrecioPorMolde);
            if (esPrecioPorMolde) {
                System.out.println("   Unidades por molde: " + unidadesStr);
                System.out.println("   Precio unitario calculado: " + precioUnidad);
            } else {
                System.out.println("   Precio unitario directo: " + precioUnidad);
            }
        }
        
        double precioTotal = cantidadProducida * precioUnidad;
        System.out.println("   Precio total calculado: " + precioTotal);
        
        double costoTotal = ParseUtils.safeParseDouble(fila.getOrDefault("Costo Total", "0").replace(",", "."));
        double costoUnitario = ParseUtils.safeParseDouble(fila.getOrDefault("Costo/U", "0").replace(",", "."));
        double ganancia = precioTotal - costoTotal;
        
        System.out.println("   Ganancia calculada: " + ganancia);

        Map<String, String> filaProduccion = new LinkedHashMap<>();
        filaProduccion.put("Código Producción", codigoProduccion);
        filaProduccion.put("Fecha", fechaFormateada);
        filaProduccion.put("Código Receta", codRecetaFinal[0]);
        filaProduccion.put("Cantidad Producida", fila.getOrDefault("Cantidad producida", ""));
        filaProduccion.put("Precio de Venta por Unidad", String.format("%.4f", precioUnidad)); // 🔧 CORREGIDO - 4 decimales
        filaProduccion.put("Mezcla", fila.getOrDefault("Mezcla", ""));
        filaProduccion.put("Producto", fila.getOrDefault("Producto", ""));
        filaProduccion.put("Costo Directo/U", String.format("%.4f", costoUnitario));
        filaProduccion.put("Costo Total", String.format("%.2f", costoTotal));
        filaProduccion.put("Ganancia Total", String.format("%.2f", ganancia));
        
        System.out.println("💾 Guardando producción con precio unitario: " + String.format("%.4f", precioUnidad));
        CrearUtils.crearFila("Produccion", filaProduccion);

        List<Map<String, String>> ingredientes = PanelIngredientesRecetaConMezclas.obtenerIngredientesModificados();
        for (Map<String, String> ing : ingredientes) {
            String codIng = ing.getOrDefault("Ingrediente", "");
            String nombreIng = VerUtils.buscarPorCodigo("Ingredientes", "Código", codIng, "Nombre");

            Map<String, String> filaIng = new LinkedHashMap<>();
            filaIng.put("Código Producción", codigoProduccion);
            filaIng.put("Código Receta", codRecetaFinal[0]);
            filaIng.put("Ingrediente", codIng);
            filaIng.put("Nombre Ingrediente", nombreIng);
            filaIng.put("Cantidad Usada", ing.getOrDefault("Cantidad", ""));
            filaIng.put("Unidad", ing.getOrDefault("Unidades", ""));
            filaIng.put("Costo Total", ing.getOrDefault("Costo", "0"));
            filaIng.put("Incluye", "✓".equals(ing.get("Check")) ? "✓" : "-");
            filaIng.put("Fecha Registro", fechaFormateada);
            CrearUtils.crearFila("ProduccionIngredientes", filaIng);
        }

        if (!guardarReceta) {
            mostrarConfirmacion("✅ Producción registrada correctamente.");
        }
        
        System.out.println("✅ Guardado completado exitosamente");
    }

    private static String convertirFechaSiEsNecesario(String fecha) {
        try {
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat salida = new SimpleDateFormat("dd/MM/yyyy");
                return salida.format(entrada.parse(fecha));
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al convertir la fecha: " + fecha);
        }
        return fecha;
    }

    private static String calcularSiguienteVersionPorProducto(String producto) {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");

        int maxVersion = recetas.stream()
                .filter(r -> producto.equalsIgnoreCase(r.getOrDefault("Producto", "")))
                .map(r -> r.getOrDefault("Versión", "VER0001").replaceAll("[^0-9]", ""))
                .mapToInt(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (Exception ex) {
                        return 1;
                    }
                }).max().orElse(1);

        return String.format("VER%04d", maxVersion + 1);
    }

    private static void mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}