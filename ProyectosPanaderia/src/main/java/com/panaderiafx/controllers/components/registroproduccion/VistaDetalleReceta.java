package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.layout.VBox;

public class VistaDetalleReceta {

    public static VBox crear(String codigoReceta, String tipo) {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panel.getChildren().addAll(
                PanelFormularioReceta.crear(codigoReceta, null),
                PanelIngredientesReceta.crear(codigoReceta)
        );
        return panel;
    }
}
