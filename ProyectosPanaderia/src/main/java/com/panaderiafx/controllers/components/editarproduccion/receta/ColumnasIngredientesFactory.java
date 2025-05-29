package com.panaderiafx.controllers.components.editarproduccion.receta;

import com.panaderiafx.utils.VerUtilsOptimized;
import com.panaderiafx.utils.componentes.CostoIngredientePorRecetaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ColumnasIngredientesFactory {

    public static TableColumn<Map<String, String>, String> columnaIngrediente() {
        TableColumn<Map<String, String>, String> col = new TableColumn<>("Ingrediente");
        col.setCellValueFactory(f -> {
            String cod = f.getValue().getOrDefault("Ingrediente", "");
            // Buscar directamente desde Excel actualizado
            String nombre = VerUtilsOptimized.verTabla("Ingredientes").stream()
                    .filter(r -> cod.equals(r.getOrDefault("Código", "")))
                    .map(r -> r.getOrDefault("Nombre", ""))
                    .findFirst()
                    .orElse("");
            return new SimpleStringProperty(nombre.isEmpty() ? cod : nombre);
        });
        return col;
    }

    public static TableColumn<Map<String, String>, String> columnaSimple(String titulo, String campo) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault(campo, "")));
        return col;
    }

    public static TableColumn<Map<String, String>, String> columnaEditableCantidad(
            ObservableList<Map<String, String>> datos,
            TextField campoTotal,
            TextField campoUnitario,
            String codProduccion,
            Map<String, String> prod,
            BiConsumer<String, Double> actualizarCostoEnTabla
    ) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>("Cantidad");
        col.setCellFactory(columna -> new TableCell<>() {
            final TextField editor = new TextField();

            {
                editor.setOnAction(e -> actualizar());
                editor.focusedProperty().addListener((obs, old, now) -> {
                    if (!now) actualizar();
                });
            }

            private void actualizar() {
                Map<String, String> fila = getTableView().getItems().get(getIndex());
                String nuevaCantidad = editor.getText().trim();
                fila.put("Cantidad", nuevaCantidad);

                double cantidad = ParseUtils.toDouble(nuevaCantidad);
                String codIng = fila.getOrDefault("Ingrediente", "");
                String unidad = fila.getOrDefault("Unidades", "");
                double nuevoCosto = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, unidad, cantidad);

                fila.put("Costo", String.format("%.2f", nuevoCosto));
                getTableView().refresh();

                actualizarTotales(datos, campoTotal, campoUnitario, codProduccion, prod, actualizarCostoEnTabla);
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    editor.setText(val);
                    setGraphic(editor);
                }
            }
        });

        col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Cantidad", "")));
        return col;
    }

    public static TableColumn<Map<String, String>, String> columnaCheck(
            ObservableList<Map<String, String>> datos,
            TextField campoTotal,
            TextField campoUnitario,
            String codProduccion,
            Map<String, String> prod,
            BiConsumer<String, Double> actualizarCostoEnTabla
    ) {
        TableColumn<Map<String, String>, String> col = new TableColumn<>("✓");
        col.setCellFactory(c -> new TableCell<>() {
            final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    String actual = fila.getOrDefault("Check", "✓");
                    fila.put("Check", actual.equals("✓") ? " " : "✓");
                    getTableView().refresh();
                    actualizarTotales(datos, campoTotal, campoUnitario, codProduccion, prod, actualizarCostoEnTabla);
                });
            }

            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, String> fila = getTableView().getItems().get(getIndex());
                    btn.setText(fila.getOrDefault("Check", "✓").equals("✓") ? "✓" : " ");
                    setGraphic(btn);
                }
            }
        });

        col.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getOrDefault("Check", "✓")));
        return col;
    }

    public static void actualizarTotales(
            List<Map<String, String>> datos,
            TextField campoTotal,
            TextField campoUnitario,
            String codProduccion,
            Map<String, String> prod,
            BiConsumer<String, Double> actualizarCostoEnTabla
    ) {
        double total = 0.0;

        for (Map<String, String> fila : datos) {
            if ("✓".equals(fila.getOrDefault("Check", "✓"))) {
                double cantidad = ParseUtils.toDouble(fila.getOrDefault("Cantidad", "0"));
                String codIng = fila.getOrDefault("Ingrediente", "");
                String unidad = fila.getOrDefault("Unidades", "");
                double costo = CostoIngredientePorRecetaUtils.calcularDesdeDatosDirectos(codIng, unidad, cantidad);
                fila.put("Costo", String.format("%.2f", costo));
                total += costo;
            }
        }

        double cantidadProducida = ParseUtils.toDouble(prod.getOrDefault("Cantidad Producida", "0"));
        double costoUnitario = (cantidadProducida > 0) ? total / cantidadProducida : 0.0;

        campoTotal.setText(String.format("%.2f", total));
        campoUnitario.setText(String.format("%.4f", costoUnitario));

        prod.put("Costo Total", String.format("%.2f", total));
        prod.put("Costo Directo/U", String.format("%.4f", costoUnitario));

        if (actualizarCostoEnTabla != null) {
            actualizarCostoEnTabla.accept(codProduccion, total);
        }
    }
}
