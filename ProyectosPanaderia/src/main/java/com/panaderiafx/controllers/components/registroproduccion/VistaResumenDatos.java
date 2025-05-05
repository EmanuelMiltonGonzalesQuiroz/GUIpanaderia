package com.panaderiafx.controllers.components.registroproduccion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class VistaResumenDatos {

    public static VBox crearVista(double ganancias, double costos, double parametros) {
        VBox contenedor = new VBox(10);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(15);
        grid.setAlignment(Pos.CENTER);

        grid.addRow(0, crearEtiqueta("GANANCIAS PRODUCCIÓN"), crearCampoValor(ganancias));
        grid.addRow(1, crearEtiqueta("COSTOS"), crearCampoValor(costos));
        grid.addRow(2, crearEtiqueta("PARAMETROS"), crearCampoValor(parametros));

        double total = ganancias - costos - parametros;
        grid.addRow(3, crearEtiqueta("TOTAL"), crearCampoValor(total));

        contenedor.getChildren().add(grid);
        return contenedor;
    }

    private static Label crearEtiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.setFont(Font.font("Arial", 14));
        etiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        etiqueta.setAlignment(Pos.CENTER_LEFT);
        etiqueta.setPrefWidth(150);
        return etiqueta;
    }

    private static TextField crearCampoValor(double valor) {
        TextField campo = new TextField(String.valueOf(valor));
        campo.setFont(Font.font("Arial", 14));
        campo.setEditable(false);
        campo.setAlignment(Pos.CENTER_RIGHT);
        campo.setPrefWidth(100);
        campo.setStyle("-fx-background-radius: 10;");
        return campo;
    }
}
