package com.panaderiafx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class Pestanas {

    private static final String[] TABLAS = {
        "Ingredientes", "Recetas", "Produccion", "Costos",
        "TasaCambio", "HistorialPrecios", "Parametros", "TabladeConversión"
    };

    public static TabPane crear() {
        TabPane tabPane = new TabPane();

        for (String nombre : TABLAS) {
            Tab tab = new Tab(nombre);
            tab.setClosable(false);
            tab.setContent(SubPestanasFactory.crear(nombre));
            tabPane.getTabs().add(tab);
        }

        return tabPane;
    }
}
