package com.panaderiafx.builder;

import com.panaderiafx.config.FormularioConfig;
import com.panaderiafx.utils.*;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FormularioBuilder {

    public static VBox crearFormulario(String tabla, Map<String, Control> camposOut) {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;");
        
        VBox formBox = new VBox(12);
        formBox.setMaxWidth(600);
        formBox.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup grupo = new ToggleGroup();
        RadioButton rbLocal = new RadioButton("Precio Local");
        RadioButton rbDolar = new RadioButton("Precio Dólar");
        rbLocal.setToggleGroup(grupo);
        rbDolar.setToggleGroup(grupo);
        rbLocal.setSelected(true);
        HBox tipoPrecio = new HBox(10, rbLocal, rbDolar);
        tipoPrecio.setAlignment(Pos.CENTER_LEFT);

        TextField tfPrecio = new TextField();
        tfPrecio.setFont(new Font(16));
        tfPrecio.setPromptText("Ingrese el precio");
        tfPrecio.setPrefWidth(500);

        camposOut.put("precio", tfPrecio);
        camposOut.put("_radio_local", rbLocal);
        camposOut.put("_radio_dolar", rbDolar);

        for (var campo : FormularioConfig.camposPorTabla.getOrDefault(tabla, List.of())) {
            if (campo.autogenerado) {
                TextField oculto = new TextField(CodigoGenerator.generar(tabla));
                oculto.setVisible(false);
                camposOut.put(campo.nombre, oculto);
                continue;
            }

            Label lbl = new Label(capitalize(campo.nombre) + ":");
            lbl.setFont(new Font(15));

            if (campo.tablaOpciones != null) {
                ComboBox<String> combo = new ComboBox<>();
                combo.getItems().addAll(ExcelDataUtils.obtenerUnicos(campo.tablaOpciones, campo.columnaOpciones));
                combo.setStyle("-fx-font-size: 14px;");
                camposOut.put(campo.nombre, combo);
                formBox.getChildren().addAll(lbl, combo);
            } else if (campo.nombre.toLowerCase().contains("precio")) {
                formBox.getChildren().addAll(new Label("Tipo de precio:"), tipoPrecio, new Label("Precio:"), tfPrecio);
            } else {
                TextField tf = new TextField();
                tf.setFont(new Font(14));
                camposOut.put(campo.nombre, tf);
                formBox.getChildren().addAll(lbl, tf);
            }
        }

        // Campo de fecha automática (oculto)
        String hoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        TextField fecha = new TextField(hoy);
        fecha.setVisible(false);
        camposOut.put("fecha", fecha);

        layout.getChildren().add(formBox);
        return layout;
    }

    private static String capitalize(String txt) {
        return txt.substring(0, 1).toUpperCase() + txt.substring(1);
    }
}
