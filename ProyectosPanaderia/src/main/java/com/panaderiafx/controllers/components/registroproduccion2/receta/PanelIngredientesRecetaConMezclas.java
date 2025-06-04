package com.panaderiafx.controllers.components.registroproduccion2.receta;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;

public class PanelIngredientesRecetaConMezclas {

    private static final List<Map<String, String>> ingredientesModificados = FXCollections.observableArrayList();

    public static VBox crear(String codigoReceta, Map<String, String> prod, String codigoProduccion,
                             double mezclas, BiConsumer<String, Double> actualizarCostoEnTabla) {

        VBox panel = PanelIngredientesEstiloUtils.crearContenedor();
        Label titulo = PanelIngredientesEstiloUtils.crearTitulo();
        panel.getChildren().add(titulo);

        List<Map<String, String>> recetasIngredientes = VerUtils.verTabla("RecetasIngredientes");
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");

        Map<String, String> mapaNombre = ingredientes.stream()
                .collect(Collectors.toMap(
                        i -> i.getOrDefault("Código", "").trim(),
                        i -> i.getOrDefault("Nombre", "").trim(),
                        (a, b) -> a
                ));

        Map<String, String> mapaUnidadIngrediente = ingredientes.stream()
                .collect(Collectors.toMap(
                        i -> i.getOrDefault("Código", "").trim(),
                        i -> i.getOrDefault("Unidad", "").trim(),
                        (a, b) -> a
                ));

        List<Map<String, String>> filtrados = recetasIngredientes.stream()
                .filter(m -> m.getOrDefault("Código receta", "").trim().equalsIgnoreCase(codigoReceta.trim()))
                .collect(Collectors.toList());

        if (filtrados.isEmpty()) {
            panel.getChildren().add(PanelIngredientesEstiloUtils.crearLabelError("❌ Receta no encontrada o sin ingredientes."));
            return panel;
        }

        double rendimiento = ParseUtils.toDouble(prod.getOrDefault("Rendimiento", "1"));
        double cantidadProducida = ParseUtils.toDouble(prod.getOrDefault("Cantidad producida", "1"));
        double factor = cantidadProducida / rendimiento;

        ingredientesModificados.clear();
        for (Map<String, String> fila : filtrados) {
            String codIng = fila.getOrDefault("Ingrediente", "").trim();
            double cantidadOriginal = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "1"));
            double cantidadEscalada = cantidadOriginal * factor;

            String unidadUsada = fila.getOrDefault("Unidades", "").trim();
            String unidadReal = mapaUnidadIngrediente.getOrDefault(codIng, unidadUsada);

            Map<String, String> nuevaFila = new java.util.HashMap<>();
            nuevaFila.put("Ingrediente", codIng);
            nuevaFila.put("Cantidad", String.format("%.4f", cantidadEscalada));
            nuevaFila.put("Unidad", unidadUsada);
            nuevaFila.put("Unidades", unidadUsada);
            nuevaFila.put("Check", "✓");
            nuevaFila.put("Costo", "0.00");

            ingredientesModificados.add(nuevaFila);
        }

        VBox panelTabla = PanelIngredientesTablaFactory.crearTabla(
                ingredientesModificados,
                mapaNombre,
                prod,
                codigoProduccion,
                actualizarCostoEnTabla
        );

        panel.getChildren().add(panelTabla);
        return panel;
    }

    public static List<Map<String, String>> obtenerIngredientesModificados() {
        return ingredientesModificados;
    }
}