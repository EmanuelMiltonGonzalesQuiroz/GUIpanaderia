package com.panaderiafx.controllers.components.editarproduccion.receta;

import com.panaderiafx.utils.componentes.ParseUtils;
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

        double total = datos.stream()
                .filter(f -> "✓".equals(f.getOrDefault("Check", "")))
                .mapToDouble(f -> ParseUtils.toDouble(f.getOrDefault("Costo", "0")))
                .sum();

        double cantidadProducida = ParseUtils.toDouble(prod.getOrDefault("Cantidad producida", "0"));
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
