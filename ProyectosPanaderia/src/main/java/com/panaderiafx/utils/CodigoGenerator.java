package com.panaderiafx.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodigoGenerator {

    public static String generarCodigo(String nombreTabla, String columnaCodigo) {
        List<String> codigosCrudos = VerUtils.verColumna(nombreTabla, columnaCodigo);

        Pattern pattern = Pattern.compile("^([A-Z]{3})(\\d{4})$");
        Map<String, Set<Integer>> codigosPorPrefijo = new HashMap<>();

        for (String codigo : codigosCrudos) {
            if (codigo == null) continue;
            Matcher matcher = pattern.matcher(codigo.trim().toUpperCase());
            if (matcher.matches()) {
                String prefijo = matcher.group(1);
                int numero = Integer.parseInt(matcher.group(2));

                codigosPorPrefijo.putIfAbsent(prefijo, new TreeSet<>());
                codigosPorPrefijo.get(prefijo).add(numero);
            }
        }

        if (codigosPorPrefijo.isEmpty()) {
            System.out.println("❌ No se encontró ningún código válido en la tabla.");
            return "ERR0000";
        }

        // Usar el primer prefijo encontrado (por ejemplo "ING")
        String prefijo = codigosPorPrefijo.keySet().iterator().next();
        Set<Integer> usados = codigosPorPrefijo.get(prefijo);


        for (int i = 1; i <= 9999; i++) {
            if (!usados.contains(i)) {
                String nuevo = prefijo + String.format("%04d", i);
                return nuevo;
            }
        }

        return prefijo + "9999";
    }
}
