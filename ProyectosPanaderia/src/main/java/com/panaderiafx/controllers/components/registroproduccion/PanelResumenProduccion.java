package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class PanelResumenProduccion extends VBox {

    private final TextField campoGanancia;
    private final TextField campoCosto;

    public PanelResumenProduccion(double ganancias, double costosDirectos, double costosIndirectos, double parametros, double total, Consumer<String> accionCallback) {
        super(10);
        setPrefWidth(400);

        campoGanancia = new TextField(String.format("BZD %.2f", ganancias));
        campoCosto = new TextField(String.format("BZD %.2f", costosDirectos));

        getChildren().addAll(
            crearBoton("GANANCIAS", campoGanancia, "#FFE082", accionCallback),
            crearBoton("COSTOS DIRECTOS", campoCosto, "#FFE082", accionCallback),
            crearBoton("COSTOS INDIRECTOS", new TextField(String.format("BZD %.2f", costosIndirectos)), "#FFE082", null),
            crearBoton("PARÁMETROS", new TextField(String.format("BZD %.2f", parametros)), "#FFE082", null),
            crearBoton("TOTAL", new TextField(String.format("BZD %.2f", total)), "#FFECB3", null)
        );
    }

    private VBox crearBoton(String texto, TextField campoValor, String color, Consumer<String> callback) {
        VBox contenedor = new VBox();
        campoValor.setEditable(false);
        campoValor.setPrefWidth(100);
        campoValor.setStyle("-fx-background-radius: 6; -fx-alignment: center-right;");

        BotonResumen btn = new BotonResumen(texto, campoValor, color);
        if (callback != null) {
            btn.setOnAction(e -> callback.accept(texto.toUpperCase().replace(" ", "_")));
        }

        contenedor.getChildren().add(btn);
        return contenedor;
    }

    public void actualizarGananciaYCostos(double ganancia, double costo) {
        campoGanancia.setText(String.format("BZD %.2f", ganancia));
        campoCosto.setText(String.format("BZD %.2f", costo));
    }
}
