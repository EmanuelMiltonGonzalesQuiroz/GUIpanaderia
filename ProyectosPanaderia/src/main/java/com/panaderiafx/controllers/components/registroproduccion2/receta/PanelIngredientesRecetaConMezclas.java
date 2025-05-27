package com.panaderiafx.controllers.components.registroproduccion2.receta;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PanelIngredientesRecetaConMezclas {

    private static final List<Map<String, String>> ingredientesModificados = new ArrayList<>();

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
                        i -> i.getOrDefault("Nombre", "").trim()
                ));

        List<Map<String, String>> filtrados = recetasIngredientes.stream()
                .filter(m -> m.getOrDefault("Código receta", "").trim().equalsIgnoreCase(codigoReceta.trim()))
                .map(HashMap::new)
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
            double cantidadOriginal = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "1"));
            double cantidadEscalada = cantidadOriginal * factor;
            fila.put("Cantidad", String.valueOf(cantidadEscalada));
            fila.put("Check", "✓");
            ingredientesModificados.add(new HashMap<>(fila));
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
