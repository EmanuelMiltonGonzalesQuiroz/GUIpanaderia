package com.panaderiafx.controllers.components.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;

public class TablaFactory {

    public static void construirColumnas(TableView<Map<String, String>> tabla, List<String> columnasVisibles) {
        tabla.getColumns().clear();
        for (String columna : columnasVisibles) {
            TableColumn<Map<String, String>, String> col = new TableColumn<>(columna);
            col.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().getOrDefault(columna, ""))
            );
            col.setSortable(true);
            tabla.getColumns().add(col);
        }
    }
}
