package com.panaderiafx.controllers.components.librosemanal;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class DetallePorDiaVista extends VBox {

    private final Map<LocalDate, VBox> panelesPorDia = new HashMap<>();
    private final Map<LocalDate, TableView<Map<String, String>>> tablasPorDia = new HashMap<>();

    public DetallePorDiaVista() {
        setSpacing(25);
        setStyle("-fx-background-color: #FFF3E0; -fx-padding: 15;");
    }

    public void actualizarSemana(LocalDate lunes) {
        getChildren().clear();
        panelesPorDia.clear();
        tablasPorDia.clear();

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = lunes.plusDays(i);
            VBox panelDia = crearPanelDia(fecha);
            panelesPorDia.put(fecha, panelDia);
            tablasPorDia.put(fecha, (TableView<Map<String, String>>) panelDia.getChildren().get(1));
        }
    }

    public TableView<Map<String, String>> getTablaPorFecha(LocalDate fecha) {
        return tablasPorDia.get(fecha);
    }

    public void mostrarSiTieneDatos(LocalDate fecha) {
        VBox panel = panelesPorDia.get(fecha);
        TableView<Map<String, String>> tabla = tablasPorDia.get(fecha);
        if (panel != null && tabla != null && !tabla.getItems().isEmpty()) {
            if (!getChildren().contains(panel)) {
                getChildren().add(panel);
            }
        }
    }

    private VBox crearPanelDia(LocalDate fecha) {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-border-color: #FF9800; -fx-border-width: 1; -fx-padding: 10;");

        String titulo = fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + fecha;
        Label dia = new Label("📅 " + titulo);
        dia.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setPlaceholder(new Label("No hay producciones registradas."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        panel.getChildren().addAll(dia, tabla);
        return panel;
    }
}
