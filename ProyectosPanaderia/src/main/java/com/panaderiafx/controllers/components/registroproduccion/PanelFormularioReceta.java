package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class PanelFormularioReceta {

    public static Node crear(String codigoReceta) {
        VBox contenedor = new VBox(10);
        contenedor.setStyle("-fx-background-color: #FFCC80; -fx-padding: 20; -fx-background-radius: 10;");

        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Map<String, String> receta = recetas.stream()
                .filter(r -> r.getOrDefault("Código", "").equalsIgnoreCase(codigoReceta))
                .findFirst()
                .orElse(null);

        if (receta == null) {
            contenedor.getChildren().add(new Label("Receta no encontrada"));
            return contenedor;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField campoCantidad = new TextField(receta.getOrDefault("Cantidad Producida", ""));
        TextField campoPrecioUnidad = new TextField(receta.getOrDefault("Precio por Unidad", ""));
        TextField campoPrecioTotal = new TextField(receta.getOrDefault("Precio General", ""));

        campoCantidad.setEditable(false);
        campoPrecioUnidad.setEditable(false);
        campoPrecioTotal.setEditable(false);

        grid.add(new Label("Cantidad Producida:"), 0, 0);
        grid.add(campoCantidad, 1, 0);
        grid.add(new Label("Precio de Venta por Unidad:"), 0, 1);
        grid.add(campoPrecioUnidad, 1, 1);
        grid.add(new Label("Precio de Venta General:"), 0, 2);
        grid.add(campoPrecioTotal, 1, 2);

        contenedor.getChildren().add(grid);
        return contenedor;
    }
}
