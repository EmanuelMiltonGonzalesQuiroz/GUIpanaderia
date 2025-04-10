package com.panaderiafx.config;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PrecioModificableBuilder {

    public static VBox crearCampo(String precioLocal, String precioDolar) {
        VBox grupo = new VBox(5);

        Label labelTipo = new Label("Tipo de precio:");
        ToggleGroup grupoTipo = new ToggleGroup();
        RadioButton rbLocal = new RadioButton("Precio Local");
        RadioButton rbDolar = new RadioButton("Precio Dólar");
        rbLocal.setToggleGroup(grupoTipo);
        rbDolar.setToggleGroup(grupoTipo);

        TextField campoPrecio = new TextField();
        campoPrecio.setPromptText("Ingrese el precio");
        campoPrecio.setPrefWidth(500);

        double local = parse(precioLocal);
        double dolar = parse(precioDolar);

        if (local > 0) {
            rbLocal.setSelected(true);
            campoPrecio.setText(precioLocal);
        } else if (dolar > 0) {
            rbDolar.setSelected(true);
            campoPrecio.setText(precioDolar);
        } else {
            rbLocal.setSelected(true); // Por defecto
        }

        grupo.getChildren().addAll(labelTipo, rbLocal, rbDolar, new Label("Precio:"), campoPrecio);
        grupo.setUserData(new PrecioData(grupoTipo, campoPrecio));
        grupo.setPadding(new Insets(10, 0, 10, 0));

        return grupo;
    }

    private static double parse(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public static class PrecioData {
        public ToggleGroup grupo;
        public TextField campo;

        public PrecioData(ToggleGroup grupo, TextField campo) {
            this.grupo = grupo;
            this.campo = campo;
        }
    }
}
