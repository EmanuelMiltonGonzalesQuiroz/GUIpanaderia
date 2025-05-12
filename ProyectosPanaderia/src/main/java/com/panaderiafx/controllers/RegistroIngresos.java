package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.registroproduccion.VistaResumenPrincipal;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RegistroIngresos {
    
    // Paneles compartidos entre vistas
    public static final VBox panelDetalleProducciones = new VBox();
    public static final VBox panelDetalleReceta = new VBox();
    public static final VBox panelIngredientes = new VBox();

    public static Node crearVista() {
        // Estilo general
        StackPane layout = new StackPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");
    
        // Contenedor horizontal principal (2 columnas visibles inicialmente)
        HBox contenedor = new HBox(30);
    
        // --------- Columna 1: Vista Resumen ---------
        Node vistaResumen = VistaResumenPrincipal.crearVista(panelDetalleProducciones, panelDetalleReceta, panelIngredientes);
    
        // --------- Columna 2: Contenedor con recetas e ingredientes (pero oculto al inicio) ---------
        VBox panelDerecho = new VBox(20);
        panelDerecho.setStyle("-fx-background-color: transparent;");
        panelDerecho.getChildren().addAll(panelDetalleReceta, panelIngredientes);
    
        panelDetalleReceta.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 20; -fx-background-radius: 10;");
        panelDetalleReceta.setSpacing(10);
    
        panelIngredientes.setStyle("-fx-background-color: #FF9800; -fx-padding: 20; -fx-background-radius: 10;");
        panelIngredientes.setSpacing(10);
    
        // --------- Columna central: Detalle de producciones ---------
        panelDetalleProducciones.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panelDetalleProducciones.setSpacing(10);
        panelDetalleReceta.setVisible(false);
        panelIngredientes.setVisible(false);

    
        contenedor.getChildren().addAll(vistaResumen, panelDetalleProducciones, panelDerecho);
        layout.getChildren().add(contenedor);
    
        return layout;
    }
}
