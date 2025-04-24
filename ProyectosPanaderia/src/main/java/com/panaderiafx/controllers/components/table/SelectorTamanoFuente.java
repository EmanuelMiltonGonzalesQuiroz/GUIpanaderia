package com.panaderiafx.controllers.components.table;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class SelectorTamanoFuente extends HBox {

    private final ComboBox<String> combo = new ComboBox<>();

    public SelectorTamanoFuente(Consumer<Integer> onCambioTamano) {
        combo.getItems().addAll("Pequeña", "Mediana", "Grande");
        combo.setValue("Pequeña");
        combo.setStyle("-fx-font-size: 16px;");
        this.setSpacing(5);
        this.getChildren().add(combo);

        combo.setOnAction(e -> {
            int px = switch (combo.getValue()) {
                case "Mediana" -> 20;
                case "Grande" -> 26;
                default -> 16; // Pequeña
            };
            onCambioTamano.accept(px);
        });
    }
}
