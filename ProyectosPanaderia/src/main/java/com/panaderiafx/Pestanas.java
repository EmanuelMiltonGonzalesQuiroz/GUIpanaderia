package com.panaderiafx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.util.LinkedHashMap;
import java.util.Map;

public class Pestanas {

    // Mapa con clave = nombre interno, valor = nombre visible en UI
    private static final Map<String, String> TABLAS = new LinkedHashMap<>();

    static {
        TABLAS.put("Ingredientes", "Ingredientes");
        TABLAS.put("Recetas", "Recetas");
        TABLAS.put("Produccion", "Producción");
        TABLAS.put("Costos", "Costos");
        TABLAS.put("TasaCambio", "Tasa de Cambio");
        TABLAS.put("HistorialPrecios", "Historial de Precios");
        TABLAS.put("Parametros", "Parámetros");
        TABLAS.put("TabladeConversión", "Tabla de Conversión");
    }

    public static TabPane crear() {
        TabPane tabPane = new TabPane();

        for (Map.Entry<String, String> entrada : TABLAS.entrySet()) {
            String nombreSistema = entrada.getKey();
            String nombreVisible = entrada.getValue();

            Tab tab = new Tab(nombreVisible);
            tab.setClosable(false);
            tab.setContent(SubPestanasFactory.crear(nombreSistema));
            tabPane.getTabs().add(tab);
        }

        return tabPane;
    }
}
