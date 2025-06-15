package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ComponentesUI {
    
    private static final String ESTILO_ETIQUETA_TITULO = 
        "-fx-background-color: #FFD54F; -fx-padding: 5 10; -fx-font-weight: bold;";
    
    private static final String ESTILO_CAMPO_TEXTO = 
        "-fx-background-color: white; -fx-padding: 5 10; -fx-border-color: #ccc; -fx-border-width: 1;";
    
    private static final String ESTILO_BOTON_PRIMARIO = 
        "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;";
    
    private static final String ESTILO_BOTON_SECUNDARIO = 
        "-fx-background-color:rgb(255, 188, 3); -fx-font-weight: bold; -fx-padding: 8 15;";
    
    private static final String ESTILO_BOTON_PELIGRO = 
        "-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;";
    
    public static Label crearEtiquetaTitulo(String texto) {
        Label label = new Label(texto);
        label.setStyle(ESTILO_ETIQUETA_TITULO);
        return label;
    }
    
    public static TextField crearCampoTexto(String valor) {
        TextField campo = new TextField(valor);
        campo.setStyle(ESTILO_CAMPO_TEXTO);
        campo.setPrefWidth(200);
        return campo;
    }
    
    public static TextField crearCampoTextoDeshabilitado(String valor) {
        TextField campo = crearCampoTexto(valor);
        campo.setEditable(false);
        campo.setStyle(ESTILO_CAMPO_TEXTO + "-fx-opacity: 0.7;");
        return campo;
    }
    
    public static TextField crearCampoNumerico(String valor) {
        TextField campo = crearCampoTexto(valor);
        // Restricción para solo números
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                campo.setText(oldVal);
            }
        });
        return campo;
    }
    
    public static Button crearBotonPrimario(String texto) {
        Button boton = new Button(texto);
        boton.setStyle(ESTILO_BOTON_PRIMARIO);
        return boton;
    }
    
    public static Button crearBotonSecundario(String texto) {
        Button boton = new Button(texto);
        boton.setStyle(ESTILO_BOTON_SECUNDARIO);
        return boton;
    }
    
    public static Button crearBotonPeligro(String texto) {
        Button boton = new Button(texto);
        boton.setStyle(ESTILO_BOTON_PELIGRO);
        return boton;
    }
    
    public static VBox crearContenedorSeccion(String titulo) {
        VBox contenedor = new VBox(10);
        contenedor.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        if (titulo != null && !titulo.isEmpty()) {
            Label lblTitulo = crearEtiquetaTitulo(titulo);
            contenedor.getChildren().add(lblTitulo);
        }
        
        return contenedor;
    }
    
    public static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public static void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public static boolean mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}