package com.panaderiafx.controllers;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import com.panaderiafx.utils.ExcelDataUtils;
import com.panaderiafx.utils.IdentificadorUtils;
import java.util.*;

public class ModificarController {

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

        VBox subFormularioContainer = new VBox(15);
        subFormularioContainer.setStyle("-fx-padding: 15;");

        cbPrincipal.setOnAction(e -> {
            subFormularioContainer.getChildren().clear();
            String seleccionado = cbPrincipal.getValue();

            List<Map<String, String>> coincidencias = registros.stream()
                .filter(r -> IdentificadorUtils.getIdentificadorPrimario(tabla, r).equals(seleccionado))
                .toList();

            FormularioBuilderModificar.crearFormularioParaRegistros(tabla, coincidencias, subFormularioContainer);
        });

        contenedor.getChildren().addAll(cbPrincipal, subFormularioContainer);

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #FFF6DC;");
        return scroll;
    }
}
