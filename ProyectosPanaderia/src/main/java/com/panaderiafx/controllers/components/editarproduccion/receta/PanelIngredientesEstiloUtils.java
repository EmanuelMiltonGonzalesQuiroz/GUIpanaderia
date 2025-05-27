package com.panaderiafx.controllers.components.editarproduccion.receta;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PanelIngredientesEstiloUtils {

    public static VBox crearContenedor() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panel.setPrefWidth(450);
        return panel;
    }

    public static Label crearTitulo() {
        Label titulo = new Label("INGREDIENTES ESCALADOS x MEZCLAS");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return titulo;
    }

    public static TextField crearCampoResumen(String valorInicial) {
        TextField campo = new TextField(valorInicial);
        campo.setEditable(false);
        return campo;
    }

    public static Label crearLabelError(String texto) {
        Label error = new Label(texto);
        error.setStyle("-fx-background-color: #FFD180; -fx-padding: 10;");
        return error;
    }
}
