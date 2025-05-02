package com.panaderiafx.controllers.components.registroproduccion;

import java.util.*;

public class TablaProduccionUtils {

    public static List<String> extraerColumnas(List<Map<String, String>> datos) {
        if (datos.isEmpty()) return List.of();
        return new ArrayList<>(datos.get(0).keySet());
    }
}
