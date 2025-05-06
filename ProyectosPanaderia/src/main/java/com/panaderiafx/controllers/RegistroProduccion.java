package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.registroproduccion.VistaResumenPrincipal;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegistroProduccion {

    // Paneles compartidos entre vistas
    public static final VBox panelDetalleProducciones = new VBox();
    public static final VBox panelDetalleReceta = new VBox();
    public static final VBox panelIngredientes = new VBox();

    public static Node crearVista() {
        // Estilo general
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        // Contenedor horizontal
        HBox contenedor = new HBox(30);

        // Estilos individuales para cada panel
        panelDetalleProducciones.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panelDetalleProducciones.setSpacing(10);
        panelDetalleProducciones.setMinWidth(500);
        panelDetalleProducciones.setPrefWidth(Region.USE_COMPUTED_SIZE);
        panelDetalleProducciones.setMaxWidth(Double.MAX_VALUE);

        panelDetalleReceta.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 20; -fx-background-radius: 10;");
        panelDetalleReceta.setSpacing(10);
        panelDetalleReceta.setMinWidth(450);
        panelDetalleReceta.setPrefWidth(Region.USE_COMPUTED_SIZE);
        panelDetalleReceta.setMaxWidth(Double.MAX_VALUE);

        // Vista izquierda: resumen
        Node vistaResumen = VistaResumenPrincipal.crearVista(panelDetalleProducciones, panelDetalleReceta, panelIngredientes);

        // Agrega todas las vistas al contenedor
        contenedor.getChildren().addAll(vistaResumen, panelDetalleProducciones, panelDetalleReceta, panelIngredientes);
        layout.getChildren().add(contenedor);

        return layout;
    }
}
