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
    private static boolean actualizando = false;

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
            if (campoPrecioRegistrado.isFocused() && !actualizando) {
                calcularDesdeRegistrado();
            }
        });

        campoPrecioU.textProperty().addListener((obs, o, n) -> {
            if (campoPrecioU.isFocused() && !actualizando) {
                calcularDesdeUnitario();
            }
        });

        campoCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                int cantidadNueva = TotalesProduccionUtils.parseInt(campoCantidad.getText());
                int cantidadBase = TotalesProduccionUtils.parseInt(produccionRef.getOrDefault("Cantidad Base", "0"));
                produccionRef.put("Cantidad Producida", String.valueOf(cantidadNueva));
                recalcularTotales();
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
                recalcularTotales();
            }
        });

        panel.getChildren().addAll(
                titulo, campoFechaBox, tipoPrecioBox, precioRegistradoBox,
                cantidadBox, precioBox, mezclaBox, productoBox,
                costoUBox, costoTotalBox, gananciaBox, botonGuardar, botonEliminar
        );

        calcularPrecioRegistradoInicial();
        return panel;
    }

    private static void calcularPrecioRegistrado() {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Map<String, String> receta = recetas.stream()
                .filter(f -> f.get("Código receta").equalsIgnoreCase(codigoRecetaActual))
                .findFirst().orElse(null);

        if (receta == null) return;

        String tipo = comboTipoPrecio.getValue();
        double precioReceta = ParseUtils.safeParseDouble(receta.getOrDefault(tipo, "0"));
        double unidades = ParseUtils.safeParseDouble(receta.getOrDefault("Unidades por Molde", "0"));
        double moldes = ParseUtils.safeParseDouble(receta.getOrDefault("Molde/Paquete", "0"));

        boolean esPaquete = unidades > 0 && moldes > 0;
        double precioUnidadEsperado = esPaquete ? precioReceta / unidades : precioReceta;

        actualizando = true;
        campoPrecioRegistrado.setText(String.format("%.4f", precioUnidadEsperado));
        campoPrecioU.setText(String.format("%.4f", precioUnidadEsperado));
        actualizando = false;

        recalcularTotales();
    }

    private static void calcularDesdeRegistrado() {
        double precioRegistrado = ParseUtils.safeParseDouble(campoPrecioRegistrado.getText());
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Map<String, String> receta = recetas.stream()
                .filter(f -> f.get("Código receta").equalsIgnoreCase(codigoRecetaActual))
                .findFirst().orElse(null);

        double unidades = ParseUtils.safeParseDouble(receta != null ? receta.getOrDefault("Unidades por Molde", "1") : "1");
        double moldes = ParseUtils.safeParseDouble(receta != null ? receta.getOrDefault("Molde/Paquete", "1") : "1");

        boolean esPaquete = unidades > 0 && moldes > 0;
        double precioUnitario = esPaquete ? precioRegistrado / unidades : precioRegistrado;

        actualizando = true;
        campoPrecioU.setText(String.format("%.4f", precioUnitario));
        actualizando = false;

        recalcularTotales();
    }

    private static void calcularDesdeUnitario() {
        double precioUnitario = ParseUtils.safeParseDouble(campoPrecioU.getText());
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Map<String, String> receta = recetas.stream()
                .filter(f -> f.get("Código receta").equalsIgnoreCase(codigoRecetaActual))
                .findFirst().orElse(null);

        double unidades = ParseUtils.safeParseDouble(receta != null ? receta.getOrDefault("Unidades por Molde", "1") : "1");
        double moldes = ParseUtils.safeParseDouble(receta != null ? receta.getOrDefault("Molde/Paquete", "1") : "1");

        boolean esPaquete = unidades > 0 && moldes > 0;
        double precioRegistrado = esPaquete ? precioUnitario * unidades : precioUnitario;

        actualizando = true;
        campoPrecioRegistrado.setText(String.format("%.4f", precioRegistrado));
        actualizando = false;

        recalcularTotales();
    }

    private static void calcularPrecioRegistradoInicial() {
        double precioUnitarioActual = ParseUtils.safeParseDouble(produccionRef.getOrDefault("Precio de Venta por Unidad", "0"));
        calcularDesdeUnitario();
    }

    private static void recalcularTotales() {
        TotalesProduccionUtils.recalcularTotales(
                campoCantidad, campoPrecioU, campoCostoTotal, campoCostoU, campoGanancia
        );

        // 🛠 Guardar los valores calculados en el mapa antes de guardar
        if (produccionRef != null) {
            produccionRef.put("Costo Total", campoCostoTotal.getText());
            produccionRef.put("Costo Directo/U", campoCostoU.getText());
            produccionRef.put("Ganancia", campoGanancia.getText());
            produccionRef.put("Precio de Venta por Unidad", campoPrecioU.getText());
            produccionRef.put("Precio Registrado", campoPrecioRegistrado.getText());
        }
    }


    public static void actualizarTotales(String cod, Double nuevoCostoTotal) {
        botonGuardar.setDisable(false);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #81C784; -fx-text-fill: black;");
    }

    private static void desactivarBotonGuardar() {
        botonGuardar.setDisable(true);
        botonGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #B0BEC5; -fx-text-fill: #555;");
    }
}
