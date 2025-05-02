package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.TablaInteractiva;
import com.panaderiafx.controllers.components.registroproduccion.CalculadorProduccion;
import com.panaderiafx.controllers.components.registroproduccion.TablaProduccionUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class RegistroProduccion {

    public static Node crearVista() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #FFF3E0;");

        Label titulo = new Label("Registro de Producción");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        ComboBox<String> modoCombo = new ComboBox<>();
        modoCombo.getItems().addAll("Diaria", "Mensual");
        modoCombo.setValue("Diaria");

        VBox contenedorTabla = new VBox();

        Button calcular = new Button("📊 Calcular resumen");
        calcular.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white; -fx-font-weight: bold;");

        calcular.setOnAction(e -> {
            String modo = modoCombo.getValue();
            List<Map<String, String>> resumen = CalculadorProduccion.generarResumen(modo);
            List<String> columnas = TablaProduccionUtils.extraerColumnas(resumen);
            TablaInteractiva tabla = new TablaInteractiva(resumen, columnas, 30, "ProducciónResumen");
            contenedorTabla.getChildren().setAll(tabla);
        });

        calcular.getOnAction().handle(null); // Ejecutar al iniciar

        layout.getChildren().addAll(titulo, modoCombo, calcular, contenedorTabla);
        return layout;
    }
}
