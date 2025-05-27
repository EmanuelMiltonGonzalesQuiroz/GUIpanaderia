package com.panaderiafx.controllers.components.editarproduccion;

import com.panaderiafx.utils.ConversorMezclaUtils;
import com.panaderiafx.utils.ConversorUtils;
import com.panaderiafx.utils.VerUtils;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;

public class FormularioEditorProduccionFactory {

    private static TextField campoCantidad;
    private static TextField campoPrecioU;
    private static TextField campoCostoTotal;
    private static TextField campoCostoU;
    private static TextField campoGanancia;
    private static TextField campoMezcla;
    private static String codigoRecetaActual;
    private static ObservableList<Map<String, String>> ingredientesEditable;
    private static Map<String, String> produccionRef;

    public static VBox crearFormulario(Map<String, String> produccion, ObservableList<Map<String, String>> ingredientes) {
        VBox panel = new VBox(20);
        panel.setPrefWidth(400);
        panel.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 25; -fx-background-radius: 10;");

        ingredientesEditable = ingredientes;
        produccionRef = produccion;
        codigoRecetaActual = produccion.get("Código Receta");
        String codigoProduccion = produccion.get("Código Producción");

        if (!produccion.containsKey("Cantidad Base")) {
            produccion.put("Cantidad Base", produccion.getOrDefault("Cantidad Producida", "0"));
        }

        Label titulo = new Label("Formulario de Edición de Producción");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #BF360C;");

        VBox campoFechaBox = crearCampoEditableConLabel("📅 Fecha:", produccion.getOrDefault("Fecha", ""));
        TextField campoFecha = (TextField) campoFechaBox.getChildren().get(1);

        VBox cantidadBox = crearCampoEditableConLabel("🔢 Cantidad producida:", produccion.getOrDefault("Cantidad Producida", ""));
        campoCantidad = (TextField) cantidadBox.getChildren().get(1);

        VBox precioBox = crearCampoEditableConLabel("💲 Precio por unidad:", produccion.getOrDefault("Precio de Venta por Unidad", ""));
        campoPrecioU = (TextField) precioBox.getChildren().get(1);

        VBox mezclaBox = crearCampoEditableConLabel("🥣 Mezcla usada:", produccion.getOrDefault("Mezcla", ""));
        campoMezcla = (TextField) mezclaBox.getChildren().get(1);

        VBox productoBox = crearCampoEditableConLabel("🍞 Producto:", produccion.getOrDefault("Producto", ""));
        TextField campoProducto = (TextField) productoBox.getChildren().get(1);

        VBox costoUBox = crearCampoSoloLecturaConLabel("📏 Costo/U:", produccion.getOrDefault("Costo Directo/U", ""));
        campoCostoU = (TextField) costoUBox.getChildren().get(1);

        VBox costoTotalBox = crearCampoSoloLecturaConLabel("💰 Costo Total:", produccion.getOrDefault("Costo Total", ""));
        campoCostoTotal = (TextField) costoTotalBox.getChildren().get(1);

        VBox gananciaBox = crearCampoSoloLecturaConLabel("📈 Ganancia Total:", produccion.getOrDefault("Ganancia", ""));
        campoGanancia = (TextField) gananciaBox.getChildren().get(1);

        campoCantidad.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                int cantidadNueva = parseInt(campoCantidad.getText());
                int cantidadBase = parseInt(produccion.getOrDefault("Cantidad Base", "0"));
                produccion.put("Cantidad Producida", String.valueOf(cantidadNueva));

                if (cantidadNueva > 0 && codigoRecetaActual != null) {
                    if (cantidadNueva != cantidadBase) {
                        System.out.printf("🔁 Cantidad producida cambiada: base=%d → nueva=%d, factor=%.4f%n",
                                cantidadBase, cantidadNueva, (double) cantidadNueva / cantidadBase);
                        double mezcla = ConversorMezclaUtils.calcularMezclasDesdeProduccion(cantidadNueva, codigoRecetaActual);
                        campoMezcla.setText(String.format("%.2f", mezcla));
                        escalarIngredientes(cantidadNueva);
                        produccion.put("Cantidad Base", String.valueOf(cantidadNueva));
                    }
                }
                recalcularTotales();
            }
        });

        campoMezcla.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                double mezcla = parseDouble(campoMezcla.getText());
                if (mezcla > 0 && codigoRecetaActual != null) {
                    int cantidadNueva = ConversorMezclaUtils.calcularProduccionDesdeMezclas(mezcla, codigoRecetaActual);
                    int cantidadBase = parseInt(produccion.getOrDefault("Cantidad Base", "0"));
                    campoCantidad.setText(String.valueOf(cantidadNueva));
                    produccion.put("Cantidad Producida", String.valueOf(cantidadNueva));
                    if (cantidadNueva != cantidadBase) {
                        System.out.printf("🔁 Mezcla modificada. Cantidad recalculada: base=%d → nueva=%d, factor=%.4f%n",
                                cantidadBase, cantidadNueva, (double) cantidadNueva / cantidadBase);
                        escalarIngredientes(cantidadNueva);
                        produccion.put("Cantidad Base", String.valueOf(cantidadNueva));
                    }
                }
                recalcularTotales();
            }
        });

        campoPrecioU.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) recalcularTotales();
        });

        Button botonGuardar = new Button("💾 Guardar Cambios");
        botonGuardar.setStyle("-fx-font-size: 14px;");
        botonGuardar.setOnAction(e -> {
            EditarProduccionUtils.editarProduccionYIngredientes(
                    codigoProduccion,
                    campoFecha,
                    campoCantidad,
                    campoPrecioU,
                    campoMezcla,
                    campoProducto
            );
        });

        Button botonEliminar = new Button("🗑 Eliminar Producción");
        botonEliminar.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #C62828; -fx-font-size: 14px;");
        botonEliminar.setOnAction(e -> {
            EditarProduccionUtils.eliminarProduccionCompleta(codigoProduccion);
        });

        panel.getChildren().addAll(
                titulo,
                campoFechaBox,
                cantidadBox,
                precioBox,
                mezclaBox,
                productoBox,
                costoUBox,
                costoTotalBox,
                gananciaBox,
                botonGuardar,
                botonEliminar
        );

        recalcularTotales();
        return panel;
    }

    private static void escalarIngredientes(int nuevaCantidad) {
        int cantidadBase = parseInt(produccionRef.getOrDefault("Cantidad Base", "0"));
        double rendimiento = ConversorMezclaUtils.obtenerRendimientoReceta(codigoRecetaActual);

        double factor;

        if (cantidadBase > 0 && !ingredientesEditable.isEmpty()) {
            factor = (double) nuevaCantidad / cantidadBase;
            System.out.printf("🔁 Escalando ingredientes desde ProduccionIngredientes: base=%d → nueva=%d, factor=%.4f%n",
                    cantidadBase, nuevaCantidad, factor);
        } else if (rendimiento > 0) {
            factor = (double) nuevaCantidad / rendimiento;
            System.out.printf("📐 No había ProduccionIngredientes. Escalando desde RecetasIngredientes con factor: %.4f%n", factor);
        } else {
            System.out.println("⚠️ No se pudo escalar los ingredientes. Factor 0.");
            return;
        }

        if (!Double.isFinite(factor) || factor <= 0) {
            System.out.printf("⚠️ Factor de escalado = %.4f. No se recalcularán ingredientes.%n", factor);
            return;
        }

        for (int i = 0; i < ingredientesEditable.size(); i++) {
            Map<String, String> fila = ingredientesEditable.get(i);

            double cantidadBaseIng = parseDouble(fila.getOrDefault("Cantidad Base", "0"));
            String codIng = fila.getOrDefault("Ingrediente", "");
            String unidad = fila.getOrDefault("Unidades", "");

            double cantidadEscalada = cantidadBaseIng * factor;
            double costoUnitario = calcularCostoUnitario(codIng, unidad);
            double costoTotal = cantidadEscalada * costoUnitario;

            fila.put("Cantidad", String.format("%.4f", cantidadEscalada));
            fila.put("Costo", String.format("%.4f", costoTotal));
            fila.put("Costo Unitario", String.format("%.4f", costoUnitario));

            ingredientesEditable.set(i, fila); // actualiza observable
        }

        System.out.println("✅ Ingredientes escalados correctamente.");

        // ✅ ESTE ES EL MOMENTO CORRECTO PARA RECARGAR LA TABLA VISUAL:
        PanelRecargaIngredientesUtils.recargarIngredientesDesdeFormulario(nuevaCantidad, produccionRef);
    }




    public static void actualizarTotales(String cod, Double nuevoCostoTotal) {
        campoCostoTotal.setText(String.format("%.2f", nuevoCostoTotal));
        if (produccionRef != null) {
            produccionRef.put("Costo Total", String.format("%.2f", nuevoCostoTotal));
        }
        recalcularTotales();
    }

    private static void recalcularTotales() {
        try {
            double cantidad = parseDouble(campoCantidad.getText());
            double precio = parseDouble(campoPrecioU.getText());
            double costoTotal = parseDouble(campoCostoTotal.getText());

            double costoU = (cantidad > 0) ? costoTotal / cantidad : 0.0;
            double ganancia = (precio * cantidad) - costoTotal;

            campoCostoU.setText(String.format("%.4f", costoU));
            campoGanancia.setText(String.format("%.2f", ganancia));
        } catch (Exception e) {
            campoCostoU.setText("0.0000");
            campoGanancia.setText("0.00");
        }
    }

    private static VBox crearCampoEditableConLabel(String labelTexto, String valor) {
        Label label = new Label(labelTexto);
        label.setStyle("-fx-font-size: 15px; -fx-text-fill: #5D4037;");
        TextField campo = new TextField(valor);
        campo.setStyle("-fx-font-size: 17px; -fx-background-color: #FFF3E0;");
        return new VBox(5, label, campo);
    }

    private static VBox crearCampoSoloLecturaConLabel(String labelTexto, String valor) {
        Label label = new Label(labelTexto);
        label.setStyle("-fx-font-size: 15px; -fx-text-fill: #5D4037;");
        TextField campo = new TextField(valor);
        campo.setEditable(false);
        campo.setStyle("-fx-font-size: 17px; -fx-background-color: #E0E0E0;");
        return new VBox(5, label, campo);
    }

    private static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseInt(String val) {
        try {
            return Integer.parseInt(val.replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static double calcularCostoUnitario(String codIngrediente, String unidadDestino) {
        List<Map<String, String>> ingredientes = VerUtils.verTabla("Ingredientes");
        Map<String, String> fila = ingredientes.stream()
                .filter(i -> codIngrediente.equalsIgnoreCase(i.get("Código")))
                .findFirst()
                .orElse(null);

        if (fila == null) return 0;

        String unidadBase = fila.getOrDefault("Unidad", "").trim();
        double precioBase = parseDouble(fila.getOrDefault("Precio Local", "0"));

        Double cantidadConvertida = ConversorUtils.convertir("Peso", unidadBase, unidadDestino, 1.0, codIngrediente);
        if (cantidadConvertida == null || cantidadConvertida == 0) return 0;

        return precioBase / cantidadConvertida;
    }
}
