package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.controllers.components.editarproduccion.helpers.EscaladoIngredientesUtils;
import com.panaderiafx.controllers.components.editarproduccion.helpers.FormularioCamposFactory;
import com.panaderiafx.controllers.components.editarproduccion.helpers.TotalesProduccionUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class FormularioEditorProduccionFactory {

    private static TextField campoCantidad, campoPrecioU, campoCostoTotal, campoCostoU, campoGanancia, campoMezcla;
    private static String codigoRecetaActual;
    private static ObservableList<Map<String, String>> ingredientesEditable;
    private static Map<String, String> produccionRef;
    private static String codigoProduccion;
    private static Runnable refrescarTabla;
    private static Button botonGuardar;

    public static VBox crearFormulario(Map<String, String> produccion,
                                       ObservableList<Map<String, String>> ingredientes,
                                       Runnable refrescarTablaIngredientes) {
        refrescarTabla = refrescarTablaIngredientes;
        return crearFormularioInterno(produccion, ingredientes);
    }

    private static VBox crearFormularioInterno(Map<String, String> produccion,
                                               ObservableList<Map<String, String>> ingredientes) {
        VBox panel = new VBox(20);
        panel.setPrefWidth(400);
        panel.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 25; -fx-background-radius: 10;");

        ingredientesEditable = ingredientes;
        produccionRef = produccion;
        codigoRecetaActual = produccion.get("Código Receta");
        codigoProduccion = produccion.get("Código Producción");

        Label titulo = new Label("Formulario de Edición de Producción");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #BF360C;");

        VBox campoFechaBox = FormularioCamposFactory.crearCampoEditableConLabel("📅 Fecha:", produccion.getOrDefault("Fecha", ""));
        TextField campoFecha = (TextField) campoFechaBox.getChildren().get(1);

        VBox cantidadBox = FormularioCamposFactory.crearCampoEditableConLabel("🔢 Cantidad producida:", produccion.getOrDefault("Cantidad Producida", ""));
        campoCantidad = (TextField) cantidadBox.getChildren().get(1);

        VBox precioBox = FormularioCamposFactory.crearCampoEditableConLabel("💲 Precio por unidad:", produccion.getOrDefault("Precio de Venta por Unidad", ""));
        campoPrecioU = (TextField) precioBox.getChildren().get(1);

        VBox mezclaBox = FormularioCamposFactory.crearCampoEditableConLabel("🥣 Mezcla usada:", produccion.getOrDefault("Mezcla", ""));
        campoMezcla = (TextField) mezclaBox.getChildren().get(1);

        VBox productoBox = FormularioCamposFactory.crearCampoEditableConLabel("🍞 Producto:", produccion.getOrDefault("Producto", ""));
        TextField campoProducto = (TextField) productoBox.getChildren().get(1);

        VBox costoUBox = FormularioCamposFactory.crearCampoSoloLecturaConLabel("📏 Costo/U:", produccion.getOrDefault("Costo Directo/U", ""));
        campoCostoU = (TextField) costoUBox.getChildren().get(1);

        VBox costoTotalBox = FormularioCamposFactory.crearCampoSoloLecturaConLabel("💰 Costo Total:", produccion.getOrDefault("Costo Total", ""));
        campoCostoTotal = (TextField) costoTotalBox.getChildren().get(1);

        VBox gananciaBox = FormularioCamposFactory.crearCampoSoloLecturaConLabel("📈 Ganancia Total:", produccion.getOrDefault("Ganancia", ""));
        campoGanancia = (TextField) gananciaBox.getChildren().get(1);

        botonGuardar = new Button("💾 Guardar Cambios");
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #81C784; -fx-text-fill: black;");
        botonGuardar.setOnAction(e -> EditarProduccionUtils.editarProduccionYIngredientes(
                codigoProduccion,
                campoFecha,
                campoCantidad,
                campoPrecioU,
                campoMezcla,
                campoProducto,
                ingredientesEditable
        ));

        Button botonEliminar = new Button("🗑 Eliminar Producción");
        botonEliminar.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-size: 14px;");
        botonEliminar.setOnAction(e -> EditarProduccionUtils.eliminarProduccionCompleta(codigoProduccion));

        campoCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                desactivarBotonGuardar();
                double cantidadNueva = ParseUtils.safeParseDouble(campoCantidad.getText());
                double cantidadBase = ParseUtils.safeParseDouble(produccion.getOrDefault("Cantidad Base", "0"));
                produccion.put("Cantidad Producida", String.valueOf((int)cantidadNueva));

                if (cantidadNueva > 0 && codigoRecetaActual != null && cantidadNueva != cantidadBase) {
                    Platform.runLater(() -> {
                        if (refrescarTabla != null) refrescarTabla.run();
                        activarBotonGuardar();
                    });
                } else {
                    activarBotonGuardar();
                }

                recalcularTotalesDesdeFormulario();
            }
        });

        campoMezcla.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                desactivarBotonGuardar();
                double mezcla = ParseUtils.safeParseDouble(campoMezcla.getText());
                if (mezcla > 0 && codigoRecetaActual != null) {
                    int cantidadNueva = EscaladoIngredientesUtils.calcularProduccionDesdeMezcla(mezcla, codigoRecetaActual);
                    double cantidadBase = ParseUtils.safeParseDouble(produccion.getOrDefault("Cantidad Base", "0"));
                    campoCantidad.setText(String.valueOf(cantidadNueva));
                    produccion.put("Cantidad Producida", String.valueOf(cantidadNueva));

                    Platform.runLater(() -> {
                        if (cantidadNueva != cantidadBase && refrescarTabla != null) refrescarTabla.run();
                        activarBotonGuardar();
                    });
                } else {
                    activarBotonGuardar();
                }

                recalcularTotalesDesdeFormulario();
            }
        });

        campoPrecioU.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                recalcularTotalesDesdeFormulario();
            }
        });

        panel.getChildren().addAll(
                titulo, campoFechaBox, cantidadBox, precioBox, mezclaBox, productoBox,
                costoUBox, costoTotalBox, gananciaBox, botonGuardar, botonEliminar
        );

        recalcularTotalesDesdeFormulario();
        return panel;
    }

    private static void recalcularTotalesDesdeFormulario() {
        // Usar ParseUtils para todos los valores numéricos
        double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
        double precioU = ParseUtils.safeParseDouble(campoPrecioU.getText());
        double costoTotal = ParseUtils.safeParseDouble(campoCostoTotal.getText());
        
        // Calcular costo por unidad
        double costoU = cantidad > 0 ? costoTotal / cantidad : 0;
        
        // Calcular ganancia total
        double ingresoTotal = cantidad * precioU;
        double gananciaTotal = ingresoTotal - costoTotal;
        
        // Actualizar campos con formato
        campoCostoU.setText(String.format("%.2f", costoU));
        campoGanancia.setText(String.format("%.2f", gananciaTotal));
        
        // Actualizar referencia de producción
        if (produccionRef != null) {
            produccionRef.put("Costo Directo/U", String.format("%.2f", costoU));
            produccionRef.put("Ganancia", String.format("%.2f", gananciaTotal));
            produccionRef.put("Precio de Venta por Unidad", String.format("%.2f", precioU));
            produccionRef.put("Cantidad Producida", String.format("%.0f", cantidad));
        }
        
        // También llamar al método original por si tiene lógica adicional
        TotalesProduccionUtils.recalcularTotales(
                campoCantidad, campoPrecioU, campoCostoTotal, campoCostoU, campoGanancia
        );
    }

    public static void actualizarTotales(String cod, Double nuevoCostoTotal) {
        // Usar ParseUtils para el nuevo costo total
        double costoTotal = nuevoCostoTotal != null ? nuevoCostoTotal : 0.0;
        campoCostoTotal.setText(String.format("%.2f", costoTotal));
        
        if (produccionRef != null) {
            produccionRef.put("Costo Total", String.format("%.2f", costoTotal));
        }
        recalcularTotalesDesdeFormulario();
    }

    private static void desactivarBotonGuardar() {
        botonGuardar.setDisable(true);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #B0BEC5; -fx-text-fill: #555;");
    }

    private static void activarBotonGuardar() {
        botonGuardar.setDisable(false);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #81C784; -fx-text-fill: black;");
    }
}