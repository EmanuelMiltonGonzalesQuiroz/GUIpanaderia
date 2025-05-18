package com.panaderiafx.controllers.components.librosemanal;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.Map;

public class TableCellSwitchEstiloFactory implements Callback<TableColumn<Map<String, String>, String>, TableCell<Map<String, String>, String>> {

    @Override
    public TableCell<Map<String, String>, String> call(TableColumn<Map<String, String>, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                Map<String, String> fila = getTableRow().getItem();
                setText(valor);

                if ("VariableDia".equals(fila.get("_tipo"))) {
                    setStyle("-fx-font-style: italic; -fx-background-color: #F0F0F0;");
                } else {
                    setStyle("");
                }
            }
        };
    }
}
