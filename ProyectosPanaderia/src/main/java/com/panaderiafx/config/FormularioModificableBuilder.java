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
        valores = MapUtils.normalizarKeys(valores);

        for (FormularioConfig.Campo campo : campos) {
            VBox grupo = new VBox(5);
            grupo.setUserData(campo.nombre);
            Label etiqueta = new Label(campo.nombre.toUpperCase());
            Node input;

            String valor = valores.getOrDefault(campo.nombre.toLowerCase(), "");

            if (campo.nombre.equalsIgnoreCase("precio")) {
                String precioLocal = valores.getOrDefault("precio", "");
                String precioDolar = valores.getOrDefault("precio_dólar", "");
                VBox precioField = PrecioModificableBuilder.crearCampo(precioLocal, precioDolar);
                nodos.add(precioField);
                continue;
            }else if (campo.tablaOpciones != null) {
                List<Map<String, String>> registros = ExcelDataUtils.obtenerTabla(campo.tablaOpciones);
                Set<String> opciones = new LinkedHashSet<>();

                for (Map<String, String> reg : registros) {
                    reg = MapUtils.normalizarKeys(reg);
                    String op = reg.getOrDefault(campo.columnaOpciones.toLowerCase(), "").trim();
                    if (!op.isEmpty()) opciones.add(op);
                }

                ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(opciones));
                combo.setEditable(true);
                combo.setValue(valor);
                input = combo;

            } else {
                TextField tf = new TextField(valor);
                tf.setPrefWidth(500);
                if (campo.autogenerado || campo.nombre.equalsIgnoreCase("codigo")) {
                    tf.setDisable(true);
                    tf.setText(valor);
                }
                input = tf;
            }

            grupo.getChildren().addAll(etiqueta, input);
            nodos.add(grupo);
        }

        return nodos;
    }
}
