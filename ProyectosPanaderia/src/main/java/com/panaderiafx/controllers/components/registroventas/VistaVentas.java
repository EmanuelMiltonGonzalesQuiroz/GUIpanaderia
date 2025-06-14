package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista principal de gestión de ventas con ancho optimizado
 * Componente contenedor que maneja las pestañas principales
 */
public class VistaVentas {
    
    private TabPane tabPane;
    private VentasService ventasService;
    
    public static Node crearVista() {
        return new VistaVentas().construir();
    }
    
    private Node construir() {
        inicializarServicios();
        return crearContenedorPrincipal();
    }
    
    private void inicializarServicios() {
        ventasService = new VentasService();
    }
    
    private VBox crearContenedorPrincipal() {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-background-color: #FF8A50; -fx-padding: 20;");
        
        // Establecer ancho mínimo para que quepa toda la interfaz
        contenedor.setPrefWidth(1400);
        contenedor.setMinWidth(1400);
        
        Label titulo = crearTitulo();
        tabPane = crearPestanas();
        
        contenedor.getChildren().addAll(titulo, tabPane);
        return contenedor;
    }
    
    private Label crearTitulo() {
        Label titulo = new Label("GESTIÓN DE VENTAS");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        return titulo;
    }
    
    private TabPane crearPestanas() {
        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: #FF8A50;");
        
        // Establecer ancho para las pestañas
        tabs.setPrefWidth(1350);
        tabs.setMinWidth(1350);
        
        Tab tabRegistrar = crearPestanaRegistrar();
        Tab tabEditar = crearPestanaEditar();
        Tab tabInventario = crearPestanaInventario();
        
        tabs.getTabs().addAll(tabRegistrar, tabEditar, tabInventario);
        return tabs;
    }
    
    private Tab crearPestanaRegistrar() {
        Tab tab = new Tab("Venta");
        tab.setClosable(false);
        tab.setContent(new RegistrarVentaPanel(ventasService).crear());
        return tab;
    }
    
    private Tab crearPestanaEditar() {
        Tab tab = new Tab("Produccion");
        tab.setClosable(false);
        tab.setContent(new EditarVentaPanel(ventasService).crear());
        return tab;
    }
    
    private Tab crearPestanaInventario() {
        Tab tab = new Tab("Inventario");
        tab.setClosable(false);
        tab.setContent(new InventarioPanel(ventasService).crear());
        return tab;
    }
}