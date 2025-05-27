package com.panaderiafx.controllers.components.editarproduccion.helpers;

import javafx.scene.control.TextField;

public class TotalesProduccionUtils {

    public static void recalcularTotales(TextField campoCantidad, TextField campoPrecioU, TextField campoCostoTotal,
                                         TextField campoCostoU, TextField campoGanancia) {
        try {
            double cantidad = parseDouble(campoCantidad.getText());
            double precio = parseDouble(campoPrecioU.getText());
            double costoTotal = parseDouble(campoCostoTotal.getText());

            double costoU = (cantidad > 0) ? costoTotal / cantidad : 0.0;
            double ganancia = (precio * cantidad) - costoTotal;

            campoCostoU.setText(String.format("%.4f", costoU));
            campoGanancia.setText(String.format("%.2f", ganancia));
        } catch (Exception e) {
            campoCostoU.setText("0.0000");
            campoGanancia.setText("0.00");
        }
    }

    public static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int parseInt(String val) {
        try {
            return Integer.parseInt(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
