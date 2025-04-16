package com.panaderiafx;

import com.panaderiafx.utils.VerUtils;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.*;

public class SubPestanasFactory {

    public static TabPane crear(String nombreTabla) {
        TabPane subTabs = new TabPane();
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónTablas");

        List<String> acciones = new ArrayList<>();

        for (Map<String, String> fila : config) {
            if (fila.getOrDefault("Tabla", "").equalsIgnoreCase(nombreTabla)) {
                if (fila.getOrDefault("Ver", "No").equalsIgnoreCase("Sí")) acciones.add("Ver");
                if (fila.getOrDefault("Crear", "No").equalsIgnoreCase("Sí")) acciones.add("Crear");
                if (fila.getOrDefault("Modificar", "No").equalsIgnoreCase("Sí")) acciones.add("Modificar");
                break;
            }
        }

        if (acciones.isEmpty()) {
            acciones = Arrays.asList("Ver", "Crear", "Modificar");
        }

        for (String accion : acciones) {
            Tab tab = new Tab(accion);
            tab.setClosable(false);
            tab.setOnSelectionChanged(e -> {
                if (tab.isSelected()) {
                    tab.setContent(ControladorFactory.getVista(accion, nombreTabla));
                }
            });
            subTabs.getTabs().add(tab);
        }

        return subTabs;
    }
}
