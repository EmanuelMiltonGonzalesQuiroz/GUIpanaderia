package com.panaderiafx.controllers.components.editarproduccion.helpers;

import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.control.TextField;

public class TotalesProduccionUtils {

    public static void recalcularTotales(TextField campoCantidad, TextField campoPrecioU, TextField campoCostoTotal,
                                         TextField campoCostoU, TextField campoGanancia) {
        try {
            double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
            double precio = ParseUtils.safeParseDouble(campoPrecioU.getText());
            double costoTotal = ParseUtils.safeParseDouble(campoCostoTotal.getText());

            double costoU = (cantidad > 0) ? costoTotal / cantidad : 0.0;
            double ganancia = (precio * cantidad) - costoTotal;

            campoCostoU.setText(String.format("%.4f", costoU));
            campoGanancia.setText(String.format("%.2f", ganancia));
        } catch (Exception e) {
            campoCostoU.setText("0.0000");
            campoGanancia.setText("0.00");
        }
    }

    // Métodos deprecated - usar ParseUtils en su lugar
    @Deprecated
    public static double parseDouble(String val) {
        return ParseUtils.safeParseDouble(val);
    }

    @Deprecated
    public static int parseInt(String val) {
        return ParseUtils.safeParseInt(val);
    }
}