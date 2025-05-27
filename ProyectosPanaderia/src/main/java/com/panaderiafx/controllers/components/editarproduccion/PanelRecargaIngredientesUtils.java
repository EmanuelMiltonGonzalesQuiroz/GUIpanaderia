package com.panaderiafx.controllers.components.editarproduccion;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BiConsumer;

public class PanelRecargaIngredientesUtils {

    private static VBox panelContenedor;
    private static ObservableList<Map<String, String>> listaIngredientes;
    private static Map<String, String> produccion;
    private static Map<String, String> mapaNombreIngredientes;
    private static BiConsumer<String, Double> actualizarCostoEnTabla;

    public static void inicializar(
            VBox contenedor,
            ObservableList<Map<String, String>> ingredientes,
            Map<String, String> prod,
            Map<String, String> nombres,
            BiConsumer<String, Double> callback
    ) {
        panelContenedor = contenedor;
        listaIngredientes = ingredientes;
        produccion = prod;
        mapaNombreIngredientes = nombres;
        actualizarCostoEnTabla = callback;
    }

    public static void recargarIngredientesDesdeFormulario(int nuevaCantidad, Map<String, String> prodActualizada) {
        produccion.put("Cantidad Producida", String.valueOf(nuevaCantidad));
        listaIngredientes.clear();
        Node nuevo = PanelIngredientesEditorFactory.crear(
                prodActualizada,
                listaIngredientes,
                actualizarCostoEnTabla,
                mapaNombreIngredientes
        );
        panelContenedor.getChildren().set(1, nuevo); // reemplaza la tabla
    }
}
