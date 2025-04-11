package com.panaderiafx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.util.*;

public class SubPestanasFactory {

    private static final Map<String, List<String>> estructura = new HashMap<>();

    static {
        estructura.put("Ingredientes", Arrays.asList("Ver", "Crear", "Modificar", "Eliminar", "Gráfico"));
        estructura.put("Recetas", Arrays.asList("Ver", "Crear", "Modificar", "Eliminar", "Gráfico"));
        estructura.put("Produccion", Arrays.asList("Ver", "Crear", "Modificar", "Eliminar"));
        estructura.put("Costos", Arrays.asList("Ver", "Crear","Gráfico"));
        estructura.put("TasaCambio", Arrays.asList("Ver", "Crear"));
        estructura.put("HistorialPrecios", Arrays.asList("Ver", "Gráfico"));
        estructura.put("Parametros", Arrays.asList("Ver", "Crear","Modificar"));
        estructura.put("TabladeConversión", Arrays.asList("Ver","Crear"));
    }

    public static TabPane crear(String nombreTabla) {
        TabPane subTabs = new TabPane();
        List<String> acciones = estructura.getOrDefault(nombreTabla, Collections.emptyList());

        for (String accion : acciones) {
            Tab tab = new Tab(accion);
            tab.setClosable(false);

            // Usa la fábrica de controladores para mostrar el contenido real
            tab.setContent(ControladorFactory.getVista(accion, nombreTabla));
            subTabs.getTabs().add(tab);
        }

        return subTabs;
    }
}
