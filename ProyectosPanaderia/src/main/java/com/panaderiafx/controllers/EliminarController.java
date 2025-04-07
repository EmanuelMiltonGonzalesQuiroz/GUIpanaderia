package com.panaderiafx.controllers;

import com.panaderiafx.config.FormularioConfig;
import com.panaderiafx.config.FormularioSoloLecturaBuilder;
import com.panaderiafx.utils.ExcelDataUtils;
import com.panaderiafx.utils.ExcelUtils;
import com.panaderiafx.utils.IdentificadorUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class EliminarController {

    public static ScrollPane mostrar(String tabla) {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-padding: 30; -fx-alignment: center;");

        List<Map<String, String>> registros = ExcelDataUtils.obtenerTabla(tabla);

        List<String> opciones = registros.stream()
                .map(r -> IdentificadorUtils.getIdentificadorPrimario(tabla, r))
                .filter(op -> op != null && !op.isBlank() && !op.equals("-"))
                .distinct()
                .toList();

        ComboBox<String> cbPrincipal = new ComboBox<>(FXCollections.observableArrayList(opciones));
        cbPrincipal.setPromptText("Seleccione un registro");

        VBox panelRegistros = new VBox(20);
        panelRegistros.setStyle("-fx-padding: 20;");

        cbPrincipal.setOnAction(e -> {
            panelRegistros.getChildren().clear();
            String seleccionado = cbPrincipal.getValue();

            List<Map<String, String>> coincidencias = registros.stream()
                    .filter(r -> IdentificadorUtils.getIdentificadorPrimario(tabla, r).equals(seleccionado))
                    .toList();

            if (coincidencias.isEmpty()) return;

            if (coincidencias.size() == 1) {
                agregarVisualUnico(tabla, coincidencias.get(0), panelRegistros);
            } else {
                GridPane grid = new GridPane();
                grid.setHgap(50);
                grid.setVgap(20);
                grid.setAlignment(Pos.CENTER);
                int col = 0, row = 0;
                for (Map<String, String> registro : coincidencias) {
                    VBox vista = new VBox(10);
                    agregarVisualUnico(tabla, registro, vista);
                    grid.add(vista, col, row);
                    col++;
                    if (col > 1) {
                        col = 0;
                        row++;
                    }
                }
                panelRegistros.getChildren().add(grid);
            }
        });

        contenedor.getChildren().addAll(cbPrincipal, panelRegistros);
        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #FFF6DC;");
        return scroll;
    }

    private static void agregarVisualUnico(String tabla, Map<String, String> registro, VBox destino) {
        var config = FormularioConfig.camposPorTabla.get(tabla);
        List<Node> campos = FormularioSoloLecturaBuilder.crearCamposSoloLectura(config, registro);

        Button eliminar = new Button("Eliminar");
        eliminar.setOnAction(e -> {
            String codigo = registro.getOrDefault("codigo", "");
            boolean exito = ExcelUtils.eliminarPorCodigo(tabla, codigo);
            FormularioConfig.mostrarMensaje(exito ? "✅ Eliminado" : "❌ No se pudo eliminar");
        });

        destino.getChildren().addAll(campos);
        destino.getChildren().add(eliminar);
    }
}
