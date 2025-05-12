package com.panaderiafx.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {

    public static String getNuevaVersion(String producto) {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Pattern pattern = Pattern.compile("VER(\\d{4})");

        Set<Integer> versionesUsadas = new TreeSet<>();

        for (Map<String, String> fila : recetas) {
            if (producto.equalsIgnoreCase(fila.getOrDefault("Producto", ""))) {
                String versionStr = fila.getOrDefault("Versión", "").trim();
                Matcher matcher = pattern.matcher(versionStr);
                if (matcher.matches()) {
                    int ver = Integer.parseInt(matcher.group(1));
                    versionesUsadas.add(ver);
                }
            }
        }

        // Buscar la menor versión libre
        for (int i = 1; i <= 9999; i++) {
            if (!versionesUsadas.contains(i)) {
                return String.format("VER%04d", i);
            }
        }

        // Si todas están ocupadas (poco probable)
        return "VER9999";
    }
}