package com.panaderiafx.config;

import com.panaderiafx.utils.ExcelDataUtils;
import com.panaderiafx.utils.MapUtils;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;

public class FormularioModificableBuilder {

    public static List<Node> crearCamposModificables(List<FormularioConfig.Campo> campos, Map<String, String> valores) {
        List<Node> nodos = new ArrayList<>();
        valores = MapUtils.normalizarKeys(valores); // Asegura claves en minúscula y sin símbolos extraños

        for (FormularioConfig.Campo campo : campos) {
            VBox grupo = new VBox(5);
            grupo.setUserData(campo.nombre);
            Label etiqueta = new Label(campo.nombre.toUpperCase());
            Node input;

            // Campo especial: PRECIO
            if (campo.nombre.equalsIgnoreCase("precio")) {
                String precioLocal = valores.getOrDefault("precio", "");
                if (precioLocal.isEmpty()) {
                    precioLocal = valores.getOrDefault("precio_local", "");
                }
                String precioDolar = valores.getOrDefault("precio_dolar", "");
                VBox precioBox = PrecioModificableBuilder.crearCampo(precioLocal, precioDolar);
                precioBox.setUserData("precio");
                nodos.add(precioBox);
                continue;
            }

            // Campo desde otra tabla (ComboBox editable)
            if (campo.tablaOpciones != null) {
                List<Map<String, String>> registros = ExcelDataUtils.obtenerTabla(campo.tablaOpciones);
                Set<String> opciones = new LinkedHashSet<>();

                for (Map<String, String> reg : registros) {
                    reg = MapUtils.normalizarKeys(reg);
                    String op = reg.getOrDefault(campo.columnaOpciones.toLowerCase(), "").trim();
                    if (!op.isEmpty()) opciones.add(op);
                }

                ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(opciones));
                combo.setEditable(true);
                combo.setValue(valores.getOrDefault(campo.nombre.toLowerCase(), ""));
                input = combo;

            } else {
                // Campo de texto simple
                TextField tf = new TextField(valores.getOrDefault(campo.nombre.toLowerCase(), ""));
                tf.setPrefWidth(500);

                if (campo.autogenerado || campo.nombre.equalsIgnoreCase("codigo")) {
                    tf.setDisable(true); // Campo solo lectura
                }

                input = tf;
            }

            grupo.getChildren().addAll(etiqueta, input);
            nodos.add(grupo);
        }

        return nodos;
    }
}
