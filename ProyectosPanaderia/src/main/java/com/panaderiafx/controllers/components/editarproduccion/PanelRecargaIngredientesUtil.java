package com.panaderiafx.controllers.components.editarproduccion;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BiConsumer;

import com.panaderiafx.controllers.components.editarproduccion.receta.PanelIngredientesTablaFactory;

public class PanelRecargaIngredientesUtil {

    private static VBox panelContenedor;
    private static ObservableList<Map<String, String>> listaIngredientes;
    private static Map<String, String> produccion;
    private static BiConsumer<String, Double> actualizarCostoEnTabla;
    private static int ultimaCantidad = -1;

    public static void inicializar(
            VBox contenedor,
            ObservableList<Map<String, String>> ingredientes,
            Map<String, String> prod,
            BiConsumer<String, Double> callback
    ) {
        panelContenedor = contenedor;
        listaIngredientes = ingredientes;
        produccion = prod;
        actualizarCostoEnTabla = callback;

        if (produccion.containsKey("Cantidad Producida")) {
            try {
                ultimaCantidad = Integer.parseInt(produccion.get("Cantidad Producida"));
            } catch (NumberFormatException e) {
                ultimaCantidad = -1;
            }
        }
    }

    public static void recargarIngredientesDesdeFormulario(int nuevaCantidad, Map<String, String> prodActualizada) {
        if (nuevaCantidad == ultimaCantidad) {
            System.out.println("⚠️ Cantidad no cambió, no se recarga ingredientes.");
            return;
        }

        ultimaCantidad = nuevaCantidad;
        produccion.put("Cantidad Producida", String.valueOf(nuevaCantidad));
        listaIngredientes.clear();

        System.out.println("🔄 Recargando ingredientes con nueva cantidad: " + nuevaCantidad);

        Node nuevaTabla = PanelIngredientesTablaFactory.crearTabla(
            listaIngredientes,
            prodActualizada,
            prodActualizada.get("Código Producción"),
            actualizarCostoEnTabla
        );

        panelContenedor.getChildren().setAll(
            new Label("Ingredientes registrados:"), nuevaTabla
        );
    }
}
