package com.panaderiafx.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public class FormularioConfig {
    public static class Campo {
        public String nombre;
        public boolean autogenerado = false;
        public String tablaOpciones = null;
        public String columnaOpciones = null;

        public Campo(String nombre) {
            this.nombre = nombre;
        }

        public Campo(String nombre, boolean autogenerado) {
            this.nombre = nombre;
            this.autogenerado = autogenerado;
        }

        public Campo(String nombre, String tablaOpciones, String columnaOpciones) {
            this.nombre = nombre;
            this.tablaOpciones = tablaOpciones;
            this.columnaOpciones = columnaOpciones;
        }
    }

    public static Map<String, List<Campo>> camposPorTabla = new HashMap<>();

    static {
        camposPorTabla.put("Ingredientes", Arrays.asList(
                new Campo("codigo", true),
                new Campo("nombre"),
                new Campo("unidad", "Ingredientes", "Unidad"),
                new Campo("precio"),
                new Campo("categoria", "Ingredientes", "Categoria") // Local o Dólar seleccionable
        ));

        camposPorTabla.put("Recetas", Arrays.asList(
                new Campo("codigo", true),
                new Campo("producto"),
                new Campo("ingrediente", "Ingredientes", "Nombre"),
                new Campo("rendimiento")
        ));

        camposPorTabla.put("Costos", Arrays.asList(
                new Campo("codigo", true),
                new Campo("descripcion"),
                new Campo("tipo"),
                new Campo("monto")
        ));

        camposPorTabla.put("TasaCambio", Arrays.asList(
                new Campo("codigo", true),
                new Campo("fecha"),
                new Campo("valor")
        ));

        camposPorTabla.put("TablaConversion", Arrays.asList(
                new Campo("codigo", true),
                new Campo("unidad_origen"),
                new Campo("unidad_destino"),
                new Campo("factor")
        ));
    }
    

    public static List<Node> crearCamposDesdeConfig(List<FormularioConfig.Campo> campos, Map<String, String> valores) {
        List<Node> nodos = new ArrayList<>();
        for (FormularioConfig.Campo campo : campos) {
            VBox grupo = new VBox(5);
            Label etiqueta = new Label(campo.nombre.toUpperCase());
            Node input;
            String valor = valores != null ? valores.getOrDefault(campo.nombre, "") : "";

            if (campo.tablaOpciones != null) {
                ComboBox<String> combo = new ComboBox<>();
                combo.setEditable(true);
                combo.getItems().add(valor);
                combo.setValue(valor);
                input = combo;
            } else {
                TextField tf = new TextField(valor);
                tf.setPrefWidth(500);
                input = tf;
            }

            grupo.getChildren().addAll(etiqueta, input);
            grupo.setUserData(campo.nombre);
            nodos.add(grupo);
        }
        return nodos;
    }

    public static Button crearBoton(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.setOnAction(e -> accion.run());
        btn.setPrefWidth(200);
        return btn;
    }

    public static Map<String, String> obtenerDatos(List<Node> campos) {
        Map<String, String> datos = new LinkedHashMap<>();
        for (Node nodo : campos) {
            if (nodo instanceof VBox grupo) {
                String nombre = (String) grupo.getUserData();
                Node input = grupo.getChildren().get(1);
                String valor = "";

                if (input instanceof TextField tf) valor = tf.getText();
                if (input instanceof ComboBox<?> cb && cb.getValue() != null) valor = cb.getValue().toString();

                datos.put(nombre, valor);
            }
        }
        return datos;
    }

    public static void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
