package com.panaderiafx.controllers.components;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class SelectorOpciones extends VBox {
    private final ToggleGroup grupo = new ToggleGroup();
    private final Map<String, RadioButton> radios = new HashMap<>();
    private final TextField input = new TextField();
    private final String nombre;

    public SelectorOpciones(List<String> opciones, String nombre, boolean mostrarInput, boolean obligatorio) {
        this.nombre = nombre;
        this.setSpacing(5);

        HBox radioBox = new HBox(10);
        for (String opcion : opciones) {
            RadioButton radio = new RadioButton(opcion);
            radio.setToggleGroup(grupo);
            radios.put(opcion, radio);
            radioBox.getChildren().add(radio);
        }

        if (!radios.isEmpty()) radios.values().iterator().next().setSelected(true);

        input.setPromptText("Ingrese valor...");
        input.setStyle("-fx-font-size: 14px;");
        input.setPrefWidth(200);

        this.getChildren().addAll(radioBox, mostrarInput ? input : new Label());
    }

    public Map<String, String> getResultados() {
        String seleccion = radios.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");

        String valor = input.getText();
        Map<String, String> map = new HashMap<>();
        if (seleccion.toLowerCase().contains("local")) {
            map.put("Precio Local", valor);
        } else {
            map.put("Precio Dólar", valor);
        }
        return map;
    }
}
