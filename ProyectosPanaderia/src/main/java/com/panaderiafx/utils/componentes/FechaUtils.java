package com.panaderiafx.utils.componentes;

public class FechaUtils {

    public static boolean coincide(String fechaFila, String seleccionada, String tipo) {
        if (fechaFila == null || seleccionada == null) return false;

        if (tipo.equalsIgnoreCase("DÍA")) {
            String[] partes = seleccionada.split("/");
            if (partes.length != 3) return false;

            String dia = partes[0].length() == 1 ? "0" + partes[0] : partes[0];
            String mes = partes[1].length() == 1 ? "0" + partes[1] : partes[1];
            String anio = partes[2];
            String formateada = dia + "/" + mes + "/" + anio;

            return fechaFila.equalsIgnoreCase(formateada) || fechaFila.equalsIgnoreCase(seleccionada);
        } else {
            String[] partesFila = fechaFila.split("/");
            String[] partesSel = seleccionada.split("/");

            if (partesFila.length < 2 || partesSel.length < 2) return false;

            String mesFila = partesFila[1].replaceFirst("^0+", "");
            String anioFila = partesFila[2];
            String mesSel = partesSel[0].replaceFirst("^0+", "");
            String anioSel = partesSel[1];

            return mesFila.equals(mesSel) && anioFila.equals(anioSel);
        }
    }
}
