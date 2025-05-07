package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.function.BiConsumer;

public class VistaDetalleReceta {

    public static VBox crear(String codigoReceta, Map<String, String> fila, BiConsumer<String, Double> actualizarGananciaEnTabla) {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panel.setPrefWidth(450);

        panel.getChildren().addAll(
                PanelFormularioReceta.crear(codigoReceta, fila, actualizarGananciaEnTabla),
                PanelIngredientesReceta.crear(codigoReceta)
        );

        return panel;
    }
}
