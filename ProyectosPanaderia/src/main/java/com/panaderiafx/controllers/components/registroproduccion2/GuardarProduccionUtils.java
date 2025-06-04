package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.controllers.components.registroproduccion2.receta.PanelIngredientesRecetaConMezclas;
import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.control.Alert;

import java.text.SimpleDateFormat;
import java.util.*;

import java.util.Locale;

public class GuardarProduccionUtils {

    public static void guardar(Map<String, String> fila, boolean guardarReceta) {
        System.out.println("🚀 Iniciando guardado de producción...");

        VerUtils.refrescarExcel();

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
                nueva.put("Cantidad", String.format(Locale.US, "%.2f", ParseUtils.safeParseDouble(ing.getOrDefault("Cantidad", "0"))));
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

        double cantidadProducida = ParseUtils.safeParseDouble(fila.getOrDefault("Cantidad producida", "0"));
        double precioRegistrado = ParseUtils.safeParseDouble(fila.getOrDefault("Precio registrado", "0"));

        String unidadesStr = fila.getOrDefault("Unidades por Molde", "").trim();
        String moldesStr = fila.getOrDefault("Molde/Paquete", "").trim();
        String precioUnidadExistente = fila.getOrDefault("Precio de Venta por Unidad", "").trim();

        double precioUnidad;
        if (!precioUnidadExistente.isEmpty() && ParseUtils.esNumero(precioUnidadExistente)) {
            precioUnidad = ParseUtils.safeParseDouble(precioUnidadExistente);
        } else {
            boolean esPrecioPorMolde = ParseUtils.esNumero(unidadesStr) && ParseUtils.esNumero(moldesStr);
            precioUnidad = esPrecioPorMolde ?
                    precioRegistrado / ParseUtils.safeParseDouble(unidadesStr) :
                    precioRegistrado;
        }

        double precioTotal = cantidadProducida * precioUnidad;
        double costoTotal = ParseUtils.safeParseDouble(fila.getOrDefault("Costo Total", "0").replace(",", "."));
        double costoUnitario = ParseUtils.safeParseDouble(fila.getOrDefault("Costo/U", "0").replace(",", "."));
        double ganancia = precioTotal - costoTotal;

        Map<String, String> filaProduccion = new LinkedHashMap<>();
        filaProduccion.put("Código Producción", codigoProduccion);
        filaProduccion.put("Fecha", fechaFormateada);
        filaProduccion.put("Código Receta", codRecetaFinal[0]);
        filaProduccion.put("Cantidad Producida", String.format(Locale.US, "%.0f", cantidadProducida));
        filaProduccion.put("Precio de Venta por Unidad", String.format(Locale.US, "%.4f", precioUnidad));
        filaProduccion.put("Mezcla", fila.getOrDefault("Mezcla", ""));
        filaProduccion.put("Producto", fila.getOrDefault("Producto", ""));
        filaProduccion.put("Costo Directo/U", String.format(Locale.US, "%.4f", costoUnitario));
        filaProduccion.put("Costo Total", String.format(Locale.US, "%.2f", costoTotal));
        filaProduccion.put("Ganancia Total", String.format(Locale.US, "%.2f", ganancia));

        CrearUtils.crearFila("Produccion", filaProduccion);

        List<Map<String, String>> ingredientes = PanelIngredientesRecetaConMezclas.obtenerIngredientesModificados();
        for (Map<String, String> ing : ingredientes) {
            String codIng = ing.getOrDefault("Ingrediente", "");
            String nombreIng = VerUtils.buscarPorCodigo("Ingredientes", "Código", codIng, "Nombre");

            double cantidadUsada = ParseUtils.safeParseDouble(ing.getOrDefault("Cantidad", "0"));
            double costoIng = ParseUtils.safeParseDouble(ing.getOrDefault("Costo", "0"));

            Map<String, String> filaIng = new LinkedHashMap<>();
            filaIng.put("Código Producción", codigoProduccion);
            filaIng.put("Código Receta", codRecetaFinal[0]);
            filaIng.put("Ingrediente", codIng);
            filaIng.put("Nombre Ingrediente", nombreIng);
            filaIng.put("Cantidad Usada", String.format(Locale.US, "%.2f", cantidadUsada));
            filaIng.put("Unidad", ing.getOrDefault("Unidades", ""));
            filaIng.put("Costo Total", String.format(Locale.US, "%.2f", costoIng));
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
