package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.controllers.components.editarproduccion.helpers.EscaladoIngredientesUtils;
import com.panaderiafx.controllers.components.editarproduccion.helpers.FormularioCamposFactory;
import com.panaderiafx.controllers.components.editarproduccion.helpers.TotalesProduccionUtils;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class FormularioEditorProduccionFactory {

    private static TextField campoCantidad, campoPrecioU, campoCostoTotal, campoCostoU, campoGanancia, campoMezcla;
    private static TextField campoPrecioRegistrado;
    private static ComboBox<String> comboTipoPrecio;
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

        comboTipoPrecio = new ComboBox<>();
        comboTipoPrecio.getItems().addAll("Precio por Mayor", "Precio Publics Supermarket");
        comboTipoPrecio.getSelectionModel().select("Precio por Mayor");
        HBox tipoPrecioBox = new HBox(new Label("💱 Tipo de Precio:"), comboTipoPrecio);
        tipoPrecioBox.setSpacing(10);

        campoPrecioRegistrado = new TextField();
        HBox precioRegistradoBox = new HBox(new Label("🧾 Precio registrado:"), campoPrecioRegistrado);
        precioRegistradoBox.setSpacing(10);

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

        // Listeners
        comboTipoPrecio.setOnAction(e -> calcularPrecioRegistrado());

        campoPrecioRegistrado.textProperty().addListener((obs, o, n) -> {
            if (campoPrecioRegistrado.isFocused()) calcularDesdeRegistrado();
        });

        // ✅ CORREGIDO: Solo recalcular totales, no modificar costo total
        campoPrecioU.textProperty().addListener((obs, o, n) -> {
            if (campoPrecioU.isFocused()) {
                recalcularTotalesDesdeFormulario();
            }
        });

        // ❌ ELIMINAR: Este listener está causando el problema
        // campoCostoTotal.textProperty().addListener((obs, o, n) -> {
        //     if (campoCostoTotal.isFocused()) {
        //         double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
        //         double total = ParseUtils.safeParseDouble(n);
        //         if (cantidad > 0) {
        //             double precioU = total / cantidad;
        //             campoPrecioU.setText(String.format("%.2f", precioU));
        //             recalcularTotalesDesdeFormulario();
        //         }
        //     }
        // });

        campoCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                int cantidadNueva = TotalesProduccionUtils.parseInt(campoCantidad.getText());
                int cantidadBase = TotalesProduccionUtils.parseInt(produccionRef.getOrDefault("Cantidad Base", "0"));
                produccionRef.put("Cantidad Producida", String.valueOf(cantidadNueva));
                recalcularTotalesDesdeFormulario();
                if (cantidadNueva != cantidadBase && refrescarTabla != null) refrescarTabla.run();
            }
        });

        campoMezcla.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                double mezcla = ParseUtils.safeParseDouble(campoMezcla.getText());
                if (mezcla > 0 && codigoRecetaActual != null) {
                    int nuevaCantidad = EscaladoIngredientesUtils.calcularProduccionDesdeMezcla(mezcla, codigoRecetaActual);
                    campoCantidad.setText(String.valueOf(nuevaCantidad));
                    produccionRef.put("Cantidad Producida", String.valueOf(nuevaCantidad));
                    if (refrescarTabla != null) refrescarTabla.run();
                }
                recalcularTotalesDesdeFormulario();
            }
        });

        panel.getChildren().addAll(
                titulo, campoFechaBox, tipoPrecioBox, precioRegistradoBox,
                cantidadBox, precioBox, mezclaBox, productoBox,
                costoUBox, costoTotalBox, gananciaBox, botonGuardar, botonEliminar
        );

        calcularPrecioRegistrado();
        return panel;
    }

    private static void calcularPrecioRegistrado() {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Map<String, String> receta = recetas.stream()
                .filter(f -> f.get("Código receta").equalsIgnoreCase(codigoRecetaActual))
                .findFirst().orElse(null);

        if (receta == null) return;

        String tipo = comboTipoPrecio.getValue();
        double precioReceta = ParseUtils.safeParseDouble(receta.getOrDefault(tipo, ""));
        double unidades = ParseUtils.safeParseDouble(receta.getOrDefault("Unidades por Molde", ""));
        double moldes = ParseUtils.safeParseDouble(receta.getOrDefault("Molde/Paquete", ""));

        boolean esPaquete = unidades > 0 && moldes > 0;
        double precioUnidadEsperado = esPaquete ? precioReceta / unidades : precioReceta;
        campoPrecioRegistrado.setText(String.format("%.4f", precioUnidadEsperado));

        double cantidad = ParseUtils.safeParseDouble(campoCantidad.getText());
        campoPrecioU.setText(String.format("%.2f", precioUnidadEsperado));
        
        // ✅ NO modificar costo total aquí, solo recalcular
        recalcularTotalesDesdeFormulario();
    }

    private static void calcularDesdeRegistrado() {
        double precioRegistrado = ParseUtils.safeParseDouble(campoPrecioRegistrado.getText());
        campoPrecioU.setText(String.format("%.2f", precioRegistrado));
        
        // ✅ NO modificar costo total aquí, solo recalcular
        recalcularTotalesDesdeFormulario();
    }

    private static void recalcularTotalesDesdeFormulario() {
        TotalesProduccionUtils.recalcularTotales(
                campoCantidad, campoPrecioU, campoCostoTotal, campoCostoU, campoGanancia
        );
    }

    public static void actualizarTotales(String cod, Double nuevoCostoTotal) {
        campoCostoTotal.setText(String.format("%.2f", nuevoCostoTotal));
        if (produccionRef != null) {
            produccionRef.put("Costo Total", String.format("%.2f", nuevoCostoTotal));
        }
        recalcularTotalesDesdeFormulario();
    }

    private static void activarBotonGuardar() {
        botonGuardar.setDisable(false);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #81C784; -fx-text-fill: black;");
    }

    private static void desactivarBotonGuardar() {
        botonGuardar.setDisable(true);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #B0BEC5; -fx-text-fill: #555;");
    }
}