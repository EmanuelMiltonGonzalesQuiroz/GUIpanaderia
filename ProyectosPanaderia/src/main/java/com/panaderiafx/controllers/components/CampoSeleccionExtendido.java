package com.panaderiafx.controllers.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.BiConsumer;

public class CampoSeleccionExtendido extends VBox {

    private final TextField campoPersonalizado = new TextField();
    private final Label seleccionLabel = new Label("Nada seleccionado");
    private final VBox tablaContenedor = new VBox();

    private final String tablaOrigen;
    private final List<String> columnasMostrar;
    private final String columnaCargar;

    private BiConsumer<String, CampoSeleccionExtendido> onSeleccionarListener;

    public CampoSeleccionExtendido(String tablaOrigen, String columnasMostrarTexto, String columnaCargar, String valorInicial) {
        this.tablaOrigen = tablaOrigen;
        this.columnaCargar = columnaCargar;
        this.columnasMostrar = Arrays.stream(columnasMostrarTexto.split(","))
                                     .map(String::trim)
                                     .filter(s -> !s.isEmpty())
                                     .toList();

        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.TOP_LEFT);

        Button botonSeleccionar = new Button("Seleccionar");
        botonSeleccionar.setOnAction(e -> {
            if (onSeleccionarListener != null) {
                onSeleccionarListener.accept(String.join(",", columnasMostrar), this);
            }
        });

        campoPersonalizado.setPromptText("O escriba su propia opción");
        campoPersonalizado.setPrefWidth(200);

        // Validar cuando pierde el foco
        campoPersonalizado.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // al perder foco
                String ingreso = campoPersonalizado.getText().trim();
                if (!ingreso.isBlank()) {
                    String mostrar = buscarValorMostrar(ingreso);
                    actualizarLabel(mostrar);
                } else {
                    seleccionLabel.setText("Nada seleccionado");
                }
            }
        });

        if (!valorInicial.isBlank()) {
            campoPersonalizado.setText(valorInicial);
            actualizarLabel(buscarValorMostrar(valorInicial));
        }

        HBox filaSuperior = new HBox(10, botonSeleccionar, campoPersonalizado);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        seleccionLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        tablaContenedor.setVisible(false);
        tablaContenedor.setManaged(false);

        this.getChildren().addAll(filaSuperior, seleccionLabel);
    }

    private String buscarValorMostrar(String valorCargar) {
        System.out.println("🔍 Buscando valorMostrar para: " + valorCargar);
        try {
            List<Map<String, String>> datos = com.panaderiafx.utils.VerUtils.verTabla(tablaOrigen);
            System.out.println("📄 Registros encontrados en tabla '" + tablaOrigen + "': " + datos.size());
    
            for (Map<String, String> fila : datos) {
                System.out.println("➡ Fila: " + fila);
                if (valorCargar.equals(fila.get(columnaCargar))) {
                    String encontrado = fila.getOrDefault(columnasMostrar.get(0), valorCargar);
                    System.out.println("✅ Match encontrado. Mostrar: " + encontrado);
                    return encontrado;
                }
            }
            System.out.println("⚠ No se encontró coincidencia con columna: " + columnaCargar);
        } catch (Exception e) {
            System.err.println("❌ Error al buscar valor mostrar: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
    
    public void setValorDesdeTabla(String valorMostrar, String valorCargar) {
        System.out.println("🆗 setValorDesdeTabla => mostrar: " + valorMostrar + ", cargar: " + valorCargar);
        campoPersonalizado.setText(valorCargar);
        actualizarLabel(valorMostrar);
    }
    
    private void actualizarLabel(String valorMostrar) {
        System.out.println("📢 Actualizando label con: " + valorMostrar);
        if (!columnasMostrar.isEmpty() && valorMostrar != null && !valorMostrar.isBlank()) {
            String bonito = columnasMostrar.get(0).substring(0, 1).toUpperCase() + columnasMostrar.get(0).substring(1);
            seleccionLabel.setText(bonito + " seleccionado: " + valorMostrar);
        } else {
            seleccionLabel.setText("❌ No se encontró un valor correspondiente.");
        }
    }
    

    public String getValorSeleccionado() {
        return campoPersonalizado.getText().trim();
    }

    public void setOnSeleccionarListener(BiConsumer<String, CampoSeleccionExtendido> listener) {
        this.onSeleccionarListener = listener;
    }

    public VBox getContenedorTabla() {
        return tablaContenedor;
    }

    public String getTabla() {
        return tablaOrigen;
    }

    public String getColumna() {
        return columnaCargar;
    }
}
