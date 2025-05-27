package com.panaderiafx.controllers.components.editarproduccion.helpers;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FormularioCamposFactory {

    public static VBox crearCampoEditableConLabel(String labelTexto, String valor) {
        Label label = new Label(labelTexto);
        label.setStyle("-fx-font-size: 15px; -fx-text-fill: #5D4037;");
        TextField campo = new TextField(valor);
        campo.setStyle("-fx-font-size: 17px; -fx-background-color: #FFF3E0;");
        return new VBox(5, label, campo);
    }

    public static VBox crearCampoSoloLecturaConLabel(String labelTexto, String valor) {
        Label label = new Label(labelTexto);
        label.setStyle("-fx-font-size: 15px; -fx-text-fill: #5D4037;");
        TextField campo = new TextField(valor);
        campo.setEditable(false);
        campo.setStyle("-fx-font-size: 17px; -fx-background-color: #E0E0E0;");
        return new VBox(5, label, campo);
    }
}
