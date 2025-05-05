package com.panaderiafx.utils.componentes;

public class ParseUtils {
    public static double toDouble(String texto) {
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
