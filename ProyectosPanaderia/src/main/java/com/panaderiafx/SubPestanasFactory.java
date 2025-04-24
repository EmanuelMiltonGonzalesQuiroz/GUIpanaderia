package com.panaderiafx;

import com.panaderiafx.utils.VerUtils;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.*;

public class SubPestanasFactory {

    public static TabPane crear(String nombreTabla, String nombreVisible, boolean vistaBasica) {
        TabPane subTabs = new TabPane();
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónTablas");

        List<String> acciones = new ArrayList<>();

        for (Map<String, String> fila : config) {
            if (fila.getOrDefault("Tabla", "").equalsIgnoreCase(nombreTabla)) {
                if (vistaBasica) {
                    if (fila.getOrDefault("Ver", "No").equalsIgnoreCase("Sí")) acciones.add("Ver");
                } else {
                    if (fila.getOrDefault("Ver", "No").equalsIgnoreCase("Sí")) acciones.add("Ver");
                    if (fila.getOrDefault("Crear", "No").equalsIgnoreCase("Sí")) acciones.add("Crear");
                    if (fila.getOrDefault("Modificar", "No").equalsIgnoreCase("Sí")) acciones.add("Modificar");
                }
                break;
            }
        }

        if (acciones.isEmpty()) {
            acciones = vistaBasica ? List.of("Ver") : List.of("Ver", "Crear", "Modificar");
        }

        for (String accion : acciones) {
            Tab tab = new Tab(accion);
            tab.setClosable(false);
            tab.setStyle("-fx-font-size: 16px;");
            tab.setContent(new javafx.scene.control.Label("Cargando..."));

            tab.setOnSelectionChanged(event -> {
                if (tab.isSelected() && tab.getContent() instanceof javafx.scene.control.Label) {
                    Node nuevaVista = ControladorFactory.getVista(accion, nombreTabla, nombreVisible);
                    tab.setContent(nuevaVista);
                }
            });

            subTabs.getTabs().add(tab);
        }

        return subTabs;
    }
}
