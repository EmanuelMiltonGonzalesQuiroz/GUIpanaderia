package com.panaderiafx.controllers;

import com.panaderiafx.utils.AccionModificar;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotonModificarFactory {

    // CAMBIA el tipo de retorno a Node
    public static Node crearBotonModificar(String tabla, Map<String, String> registro, List<Node> campos) {
        Button boton = new Button("Modificar");
        Label error = new Label();
        error.setStyle("-fx-text-fill: red;");

        boton.setOnAction(e -> {
            Map<String, Node> mapaCampos = new HashMap<>();

            for (Node nodo : campos) {
                if (!(nodo instanceof VBox grupo)) continue;
                Object userKey = grupo.getUserData();
                if (userKey == null) continue;

                String clave = userKey.toString().toLowerCase();

                // Guardamos directamente el VBox para campos especiales como "precio"
                if (clave.equals("precio")) {
                    mapaCampos.put(clave, grupo);
                    continue;
                }

                for (Node n : grupo.getChildren()) {
                    if (n instanceof TextField tf) {
                        mapaCampos.put(clave, tf);
                        break;
                    } else if (n instanceof ComboBox<?> cb) {
                        mapaCampos.put(clave, cb);
                        break;
                    }
                }
            }

            AccionModificar.ejecutar(tabla, mapaCampos, error);
        });

        return new VBox(10, boton, error); // ✅ ahora sí correcto
    }
}
