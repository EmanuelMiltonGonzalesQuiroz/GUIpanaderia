package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.controllers.components.editarproduccion.helpers.EscaladoIngredientesUtils;
import com.panaderiafx.controllers.components.editarproduccion.receta.PanelIngredientesTablaFactory;
import com.panaderiafx.utils.VerUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PanelIngredientesEditorFactory {

    private static Map<String, String> cacheNombresIngredientes;
    private static String cacheCodigoProduccion = "";
    private static int ultimaCantidadActual = -1;
    private static VBox tablaAnterior;

    public static VBox crear(Map<String, String> produccion,
                             ObservableList<Map<String, String>> datos,
                             BiConsumer<String, Double> actualizarCostoEnTabla) {

        long tInicio = System.currentTimeMillis();

        String codigoProduccion = produccion.get("Código Producción");
        String codigoReceta = produccion.get("Código Receta");
        int cantidadActual = parseInt(produccion.getOrDefault("Cantidad Producida", "0"));

        // ⚠️ Si no cambió nada y hay tabla previa, se reutiliza
        if (cantidadActual == ultimaCantidadActual && cacheCodigoProduccion.equals(codigoProduccion) && tablaAnterior != null) {
            System.out.println("♻️ Reutilizando tabla de ingredientes sin cambios.");
            return tablaAnterior;
        }

        // ⚡ Nombres de ingredientes sí usan caché porque no cambian
        if (cacheNombresIngredientes == null) {
            System.out.println("📥 Cargando nombres de ingredientes...");
            cacheNombresIngredientes = VerUtils.verTablaConCache("Ingredientes").stream()
                    .filter(f -> f.containsKey("Código") && f.containsKey("Nombre"))
                    .collect(Collectors.toMap(
                            f -> f.get("Código"),
                            f -> f.get("Nombre"),
                            (a, b) -> a
                    ));
        }

        // 🚫 Datos de producción se deben leer sin caché para ver cambios
        VerUtils.forzarActualizacion("Produccion");
        List<Map<String, String>> filaProduccion = VerUtils.verFilas("Produccion", Map.of("Código Producción", codigoProduccion));
        int cantidadBase = filaProduccion.isEmpty() ? 0 : parseInt(filaProduccion.get(0).getOrDefault("Cantidad Producida", "0"));
        produccion.put("Cantidad Base", String.valueOf(cantidadBase));

        // 🔁 Leer ingredientes desde ProduccionIngredientes si hay datos válidos
        VerUtils.forzarActualizacion("ProduccionIngredientes");
        List<Map<String, String>> ingredientesProduccion = VerUtils.verFilas("ProduccionIngredientes", Map.of("Código Producción", codigoProduccion));
        boolean usarProduccion = !ingredientesProduccion.isEmpty() && contieneCantidadesUsadas(ingredientesProduccion);

        List<Map<String, String>> fuente = usarProduccion
                ? ingredientesProduccion
                : VerUtils.verFilas("RecetasIngredientes", Map.of("Código receta", codigoReceta));

        cacheCodigoProduccion = codigoProduccion;
        System.out.println(usarProduccion ? "🔁 Cargando desde ProduccionIngredientes..." : "📄 Usando RecetasIngredientes...");

        datos.clear();
        fuente.forEach(fila -> datos.add(crearFilaIngrediente(fila)));

        double rendimiento = EscaladoIngredientesUtils.obtenerRendimientoReceta(codigoReceta);
        double factor = calcularFactor(cantidadActual, cantidadBase, rendimiento, cantidadBase > 0);

        if (cantidadActual != ultimaCantidadActual && factor != 1.0) {
            long tEscalar = System.currentTimeMillis();
            EscaladoIngredientesUtils.actualizarIngredientesDesdeCantidad(
                    cantidadActual, cantidadBase, codigoReceta, datos, produccion
            );
            System.out.printf("⏱️ Tiempo en escalado: %d ms%n", System.currentTimeMillis() - tEscalar);
            imprimirIngredientes(datos);
            ultimaCantidadActual = cantidadActual;
        } else {
            System.out.println("⏭️ Factor no cambió o es 1.0, se omite recalculado de ingredientes.");
        }

        VBox tabla = PanelIngredientesTablaFactory.crearTabla(datos, produccion, codigoProduccion, actualizarCostoEnTabla);
        VBox contenedor = new VBox(10, new Label("Ingredientes registrados:"), tabla);
        contenedor.setPrefWidth(500);
        contenedor.setStyle("-fx-padding: 20; -fx-background-color: #FFFDE7; -fx-background-radius: 10;");
        tablaAnterior = contenedor;

        PanelRecargaIngredientesUtil.inicializar(contenedor, datos, produccion, actualizarCostoEnTabla);

        if (!datos.isEmpty()) {
            produccion.put("__ingredientes", "ok");
        }

        System.out.printf("⏱️ Tiempo en PanelIngredientesEditorFactory: %d ms%n", System.currentTimeMillis() - tInicio);
        return contenedor;
    }

    private static Map<String, String> crearFilaIngrediente(Map<String, String> fila) {
        String cod = fila.getOrDefault("Ingrediente", "");
        return new LinkedHashMap<>(Map.of(
                "Ingrediente", cod,
                "Nombre Ingrediente", cacheNombresIngredientes.getOrDefault(cod, ""),
                "Unidades", fila.getOrDefault("Unidad", fila.getOrDefault("Unidades", "")),
                "Cantidad Base", fila.getOrDefault("Cantidad Usada", fila.getOrDefault("Cantidad", "0")),
                "Cantidad", fila.getOrDefault("Cantidad Usada", fila.getOrDefault("Cantidad", "0")),
                "Costo", fila.getOrDefault("Costo Total", "0"),
                "Costo Unitario", fila.getOrDefault("Costo Unitario", "0"),
                "Check", fila.getOrDefault("Incluye", "✓")
        ));
    }

    private static void imprimirIngredientes(List<Map<String, String>> datos) {
        System.out.println("📦 Ingredientes escalados generados:");
        datos.forEach(fila -> System.out.printf("   - [%s] %s %s → Costo: %s%n",
                fila.getOrDefault("Ingrediente", "?"),
                fila.getOrDefault("Cantidad", "?"),
                fila.getOrDefault("Unidades", "?"),
                fila.getOrDefault("Costo", "?")));
    }

    private static double calcularFactor(int cantidadActual, int cantidadBase, double rendimiento, boolean usarProduccion) {
        if (usarProduccion && cantidadBase > 0) return (double) cantidadActual / cantidadBase;
        if (rendimiento > 0) return cantidadActual / rendimiento;
        return 1.0;
    }

    private static boolean contieneCantidadesUsadas(List<Map<String, String>> lista) {
        return lista.stream().anyMatch(f -> parseDouble(f.getOrDefault("Cantidad Usada", "0")) > 0);
    }

    private static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseInt(String val) {
        try {
            return Integer.parseInt(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
