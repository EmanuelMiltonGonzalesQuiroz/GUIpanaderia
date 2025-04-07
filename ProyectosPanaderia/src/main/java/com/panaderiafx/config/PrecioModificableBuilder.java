package com.panaderiafx.config;

import java.util.Map;

import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PrecioModificableBuilder {

    public static VBox crearCampo(String valorLocal, String valorDolar) {
        VBox grupo = new VBox(5);
        grupo.setUserData("precio");

        ToggleGroup grupoOpciones = new ToggleGroup();

        RadioButton rbLocal = new RadioButton("Precio Local");
        RadioButton rbDolar = new RadioButton("Precio Dólar");

        rbLocal.setToggleGroup(grupoOpciones);
        rbDolar.setToggleGroup(grupoOpciones);
        rbLocal.setUserData("local");
        rbDolar.setUserData("dolar");

        TextField campo = new TextField();
        campo.setPromptText("Ingrese el precio");
        campo.setPrefWidth(500);

        double numLocal = parseDouble(valorLocal);
        double numDolar = parseDouble(valorDolar);

        if (numDolar > 0 && numDolar < numLocal) {
            rbDolar.setSelected(true);
            campo.setText(valorDolar);
        } else {
            rbLocal.setSelected(true);
            campo.setText(valorLocal);
        }

        grupo.getChildren().addAll(
                new Label("Tipo de precio:"),
                new HBox(15, rbLocal, rbDolar),
                new Label("Precio:"),
                campo
        );

        grupo.setUserData(Map.of(
                "campo", campo,
                "tipo", grupoOpciones
        ));

        return grupo;
    }

    private static double parseDouble(String texto) {
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
