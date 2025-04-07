package com.panaderiafx.config;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;

public class FormularioSoloLecturaBuilder {

    public static List<Node> crearCamposSoloLectura(List<FormularioConfig.Campo> campos, Map<String, String> valores) {
        List<Node> nodos = new ArrayList<>();
        for (FormularioConfig.Campo campo : campos) {
            VBox grupo = new VBox(5);
            Label etiqueta = new Label(campo.nombre.toUpperCase());
            String valor = valores.getOrDefault(campo.nombre.toLowerCase(), "");

            TextField campoTexto = new TextField(valor);
            campoTexto.setDisable(true);
            campoTexto.setPrefWidth(500);

            grupo.getChildren().addAll(etiqueta, campoTexto);
            nodos.add(grupo);
        }
        return nodos;
    }
}
