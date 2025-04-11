package com.panaderiafx.controllers.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class FiltroBusqueda extends VBox {

    public FiltroBusqueda(Consumer<String> onFiltrar) {
        TextField campoBusqueda = new TextField();
        campoBusqueda.setPromptText("🔍 Buscar...");
        campoBusqueda.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-background-color: #FFF9C4;" +
            "-fx-padding: 10px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-color: #FBC02D;" +
            "-fx-border-width: 1.5px;"
        );
        campoBusqueda.setMaxWidth(Double.MAX_VALUE);

        // ✅ Mostrar todo cuando está vacío
        campoBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                onFiltrar.accept(""); // Muestra todo
            } else {
                onFiltrar.accept(newVal);
            }
        });

        this.getChildren().add(campoBusqueda);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: transparent;");
    }
}
