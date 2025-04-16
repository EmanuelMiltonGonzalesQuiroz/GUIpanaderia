package com.panaderiafx;

import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.*;

public class Pestanas {

    public static TabPane crear() {
        TabPane tabPane = new TabPane();

        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónTablas");

        Set<String> agregados = new HashSet<>();

        for (Map<String, String> fila : config) {
            String nombreSistema = fila.get("Tabla");
            String nombreVisible = fila.getOrDefault("Nombre Visible", nombreSistema);
            String mostrar = fila.getOrDefault("Mostrar", "No");

            if (mostrar.equalsIgnoreCase("Sí")) {
                Tab tab = new Tab(nombreVisible);
                tab.setClosable(false);
                tab.setContent(SubPestanasFactory.crear(nombreSistema));
                tabPane.getTabs().add(tab);
                agregados.add(nombreSistema.toLowerCase());
            }
        }

        // Por si existen hojas sin configuración
        List<String> hojas = VerUtils.obtenerNombresTablas();
        for (String hoja : hojas) {
            if (!agregados.contains(hoja.toLowerCase())) {
                Tab tab = new Tab(hoja);
                tab.setClosable(false);
                tab.setContent(SubPestanasFactory.crear(hoja));
                tabPane.getTabs().add(tab);
            }
        }

        return tabPane;
    }
}
