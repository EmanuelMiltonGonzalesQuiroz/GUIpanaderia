package com.panaderiafx.controllers.components.registroproduccion;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostosDirectosPorRecetaUtils;
import com.panaderiafx.utils.componentes.FechaUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.cache.CacheCostosDirectosUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PanelDetalleProducciones {

    public static void mostrar(VBox contenedor, String fecha, String tipo, BiConsumer<Double, Double> actualizarTotales, Consumer<String> accionEditar) {
        TableView<Map<String, Object>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Map<String, Object>, String> colReceta = new TableColumn<>("RECETA");
        colReceta.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("Receta")));

        TableColumn<Map<String, Object>, String> colFecha = new TableColumn<>("FECHA");
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("Fecha")));

        TableColumn<Map<String, Object>, Double> colGanancia = new TableColumn<>("GANANCIA");
        colGanancia.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>((Double) data.getValue().get("Ganancia")));

        TableColumn<Map<String, Object>, Double> colCosto = new TableColumn<>("COSTO");
        colCosto.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>((Double) data.getValue().get("Costo")));

        TableColumn<Map<String, Object>, Boolean> colCheck = new TableColumn<>("CHECK");
        colCheck.setCellValueFactory(data -> (SimpleBooleanProperty) data.getValue().get("Check"));
        colCheck.setCellFactory(tc -> {
            CheckBoxTableCell<Map<String, Object>, Boolean> cell = new CheckBoxTableCell<>();
            cell.setSelectedStateCallback(index -> {
                Map<String, Object> item = tabla.getItems().get(index);
                return (SimpleBooleanProperty) item.get("Check");
            });
            return cell;
        });

        TableColumn<Map<String, Object>, Void> colEditar = new TableColumn<>("EDITAR");
        colEditar.setCellFactory(param -> new TableCell<>() {
            final Button btn = new Button("EDITAR");
            {
                btn.setOnAction(event -> {
                    Map<String, Object> datos = getTableView().getItems().get(getIndex());
                    String cod = (String) datos.get("Codigo");
                    accionEditar.accept(cod);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabla.getColumns().addAll(colReceta, colFecha, colGanancia, colCosto, colCheck, colEditar);

        List<Map<String, String>> produccion = VerUtils.verTabla("Produccion").stream()
                .filter(p -> FechaUtils.coincide(p.get("Fecha"), fecha, tipo))
                .collect(Collectors.toList());

        Map<String, String> mapaCodANombre = VerUtils.verTabla("Recetas").stream()
                .collect(Collectors.toMap(
                        r -> r.getOrDefault("Código receta", "").trim(),
                        r -> r.getOrDefault("Producto", "").trim(),
                        (a, b) -> a
                ));

        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        for (Map<String, String> fila : produccion) {
            String cod = fila.getOrDefault("Código receta", "");
            double cant = ParseUtils.toDouble(fila.getOrDefault("Cantidad producida", "0"));
            double precioU = ParseUtils.toDouble(fila.getOrDefault("Precio de Venta por Unidad", "0"));
            double ganancia = cant * precioU;
            double costo = CacheCostosDirectosUtils.contiene(cod, cant)
                    ? CacheCostosDirectosUtils.obtener(cod, cant)
                    : CostosDirectosPorRecetaUtils.calcular(cod, cant);

            CacheCostosDirectosUtils.guardar(cod, cant, costo);
            SimpleBooleanProperty check = new SimpleBooleanProperty(true);
            check.addListener((obs, old, val) -> recalcular(datos, actualizarTotales));

            Map<String, Object> filaNueva = new HashMap<>();
            filaNueva.put("Receta", mapaCodANombre.getOrDefault(cod, cod));
            filaNueva.put("Fecha", fila.getOrDefault("Fecha", ""));
            filaNueva.put("Ganancia", ganancia);
            filaNueva.put("Costo", costo);
            filaNueva.put("Check", check);
            filaNueva.put("Codigo", cod);
            datos.add(filaNueva);
        }

        tabla.setItems(datos);
        recalcular(datos, actualizarTotales);

        contenedor.getChildren().add(tabla);
    }

    private static void recalcular(List<Map<String, Object>> datos, BiConsumer<Double, Double> actualizar) {
        double sumaGanancia = datos.stream()
                .filter(d -> ((SimpleBooleanProperty) d.get("Check")).get())
                .mapToDouble(d -> (Double) d.get("Ganancia")).sum();

        double sumaCosto = datos.stream()
                .filter(d -> ((SimpleBooleanProperty) d.get("Check")).get())
                .mapToDouble(d -> (Double) d.get("Costo")).sum();

        actualizar.accept(sumaGanancia, sumaCosto);
    }
}
