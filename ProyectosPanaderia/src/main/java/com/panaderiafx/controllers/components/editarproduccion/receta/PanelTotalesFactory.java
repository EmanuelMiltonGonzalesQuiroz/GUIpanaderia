package com.panaderiafx.controllers.components.editarproduccion.receta;

import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class PanelTotalesFactory {

    public static VBox crearBloqueTotales(List<Map<String, String>> datos,
                                          String codProduccion,
                                          Map<String, String> prod,
                                          BiConsumer<String, Double> actualizar) {

        TextField campoTotal = crearCampoResumen("0.00");
        TextField campoUnitario = crearCampoResumen("0.0000");

        VBox totales = new VBox(5,
                new HBox(10, new Label("Costo Total:"), campoTotal),
                new HBox(10, new Label("Costo x Unidad:"), campoUnitario)
        );
        totales.setStyle("-fx-background-color: #FFCC80; -fx-padding: 10;");

        actualizarTotales(datos, campoTotal, campoUnitario, codProduccion, prod, actualizar);
        return totales;
    }

    public static void actualizarTotales(List<Map<String, String>> datos,
                                         TextField campoTotal,
                                         TextField campoUnitario,
                                         String codProduccion,
                                         Map<String, String> prod,
                                         BiConsumer<String, Double> actualizar) {

        double total = 0.0;

        for (Map<String, String> fila : datos) {
            if (!"✓".equals(fila.getOrDefault("Check", "✓"))) continue;

            double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "1"));
            String codIng = fila.getOrDefault("Ingrediente", "");
            String unidad = fila.getOrDefault("Unidades", "");

            double costo = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, unidad, cantidad);
            fila.put("Costo", String.format("%.2f", costo));
            total += costo;
        }

        double cantidadProducida = ParseUtils.toDouble(prod.getOrDefault("Cantidad producida", "0"));
        double cantidadBase = ParseUtils.toDouble(prod.getOrDefault("Cantidad Base", String.valueOf(cantidadProducida)));

        if (cantidadBase != 0 && cantidadProducida != cantidadBase) {
            double factor = cantidadProducida / cantidadBase;
            System.out.printf("🔁 Cantidad producida cambiada: base=%s → nueva=%s, factor=%.4f%n",
                    cantidadBase, cantidadProducida, factor);
        }

        double costoUnitario = (cantidadProducida > 0) ? total / cantidadProducida : 0.0;

        campoTotal.setText(String.format("%.2f", total));
        campoUnitario.setText(String.format("%.4f", costoUnitario));

        prod.put("Costo directo", String.format("%.2f", total));
        prod.put("Costo/U", String.format("%.4f", costoUnitario));

        if (actualizar != null) {
            actualizar.accept(codProduccion, total);
        }
    }

    public static TextField crearCampoResumen(String valorInicial) {
        TextField campo = new TextField(valorInicial);
        campo.setEditable(false);
        return campo;
    }
}
