package com.panaderiafx.controllers.components;

import javafx.scene.control.ComboBox;

import java.util.List;

import com.panaderiafx.utils.VerUtils;

public class ListaSeleccion extends ComboBox<String> {

    public ListaSeleccion(String tabla, String columna) {
        super();
        // TODO: deberías llenar desde VerUtils.verColumna(tabla, columna) y eliminar duplicados
        List<String> opciones = VerUtils.verColumna(tabla, columna).stream().distinct().toList();
        this.getItems().addAll(opciones);
        if (!opciones.isEmpty()) this.setValue(opciones.get(0));
        this.setStyle("-fx-font-size: 14px;");
        this.setPrefWidth(200);
    }

    public String getValorSeleccionado() {
        return this.getValue();
    }
}
