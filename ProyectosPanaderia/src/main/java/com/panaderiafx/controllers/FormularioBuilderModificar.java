package com.panaderiafx.controllers;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import com.panaderiafx.config.FormularioConfig;
import com.panaderiafx.config.FormularioModificableBuilder;

import java.util.*;

public class FormularioBuilderModificar {

    public static void crearFormularioParaRegistros(String tabla, List<Map<String, String>> registros, VBox destino) {
        if (registros.size() == 1) {
            VBox centrado = new VBox(15);
            centrado.setAlignment(Pos.CENTER);
            centrado.setMaxWidth(600);
            centrado.setStyle("-fx-background-color: transparent;");
            crearFormularioIndividual(tabla, registros.get(0), centrado);

            HBox contenedor = new HBox(centrado);
            contenedor.setAlignment(Pos.CENTER);
            destino.getChildren().add(contenedor);
        } else {
            GridPane grid = new GridPane();
            grid.setHgap(50);
            grid.setVgap(20);
            grid.setAlignment(Pos.CENTER);
            int col = 0, row = 0;
            for (Map<String, String> registro : registros) {
                VBox formBox = new VBox(10);
                crearFormularioIndividual(tabla, registro, formBox);
                grid.add(formBox, col, row);
                col++;
                if (col > 1) {
                    col = 0;
                    row++;
                }
            }
            destino.getChildren().add(grid);
        }
    }

    private static void crearFormularioIndividual(String tabla, Map<String, String> registro, VBox destino) {
        
        var config = FormularioConfig.camposPorTabla.get(tabla);
        List<Node> campos = FormularioModificableBuilder.crearCamposModificables(config, registro);
    
        destino.getChildren().addAll(campos);
        destino.getChildren().add(BotonModificarFactory.crearBotonModificar(tabla, registro, campos));
    }
    
}
