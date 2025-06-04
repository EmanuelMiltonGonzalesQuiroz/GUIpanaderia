package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.controllers.components.registroproduccion2.receta.PanelIngredientesRecetaConMezclas;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VistaRegistroProduccion {

    public static Node crearVista() {
        VBox contenedor = new VBox(20);
        contenedor.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 30;");

        Label titulo = new Label("Registrar Nueva Producción");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        PanelSelectorRecetaConTabla selector = new PanelSelectorRecetaConTabla();
        selector.setPrefWidth(600);
        selector.setPrefHeight(400);

        FormularioNuevaReceta formExtra = new FormularioNuevaReceta();
        VBox panelFormularioReceta = new VBox();
        VBox panelIngredientesReceta = new VBox();

        HBox panelHorizontalDerecho = new HBox(20, panelFormularioReceta, panelIngredientesReceta);
        panelFormularioReceta.setPrefWidth(300);
        panelIngredientesReceta.setPrefWidth(350);
        HBox.setHgrow(panelFormularioReceta, Priority.ALWAYS);
        HBox.setHgrow(panelIngredientesReceta, Priority.ALWAYS);

        VBox columnaDerecha = new VBox(20, panelHorizontalDerecho);
        columnaDerecha.setPrefWidth(700);

        HBox detalle = new HBox(40, selector, columnaDerecha);
        HBox.setHgrow(selector, Priority.NEVER);
        HBox.setHgrow(columnaDerecha, Priority.ALWAYS);

        Button btnGuardar = new Button("💾 GUARDAR PRODUCCIÓN");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");

        Button btnActualizar = new Button("🔁 ACTUALIZAR");
        btnActualizar.setStyle("-fx-background-color: #FFA726; -fx-text-fill: black; -fx-font-weight: bold;");
        btnActualizar.setOnAction(e -> {
            VerUtils.refrescarExcel();
            selector.recargar();
        });

        HBox filaBotones = new HBox(10, btnGuardar, btnActualizar);

        btnGuardar.setOnAction(e -> {
            String codReceta = selector.getCodigoRecetaSeleccionado();
            String fecha = selector.getFechaSeleccionada();
            String nombreProducto = VerUtils.buscarPorCodigo("Recetas", "Código receta", codReceta, "Producto");
            String cantidad = formExtra.getCantidad();
            String precioRegistrado = formExtra.getPrecioRegistrado();
            String precioUnitario = formExtra.getPrecioUnitario();
            String total = formExtra.getPrecioTotal();
            String mezcla = formExtra.getMezclas();

            if (codReceta == null || fecha.isEmpty() || cantidad.isEmpty() || precioRegistrado.isEmpty() || total.isEmpty()) {
                mostrarError("Complete todos los campos antes de guardar.");
                return;
            }

            double cantidadNum = ParseUtils.toDouble(cantidad);
            double precioRegistradoNum = ParseUtils.toDouble(precioRegistrado);
            double totalNum = ParseUtils.toDouble(total);

            if (cantidadNum == 0 || precioRegistradoNum == 0 || totalNum == 0) {
                mostrarError("Cantidad, precio registrado y total deben ser mayores a cero.");
                return;
            }

            String textoOriginal = btnGuardar.getText();
            String estiloOriginal = btnGuardar.getStyle();

            btnGuardar.setDisable(true);
            btnGuardar.setText("⏳ GUARDANDO...");
            btnGuardar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");

            javafx.animation.RotateTransition rotacion = new javafx.animation.RotateTransition(javafx.util.Duration.millis(1000), btnGuardar);
            rotacion.setByAngle(360);
            rotacion.setCycleCount(javafx.animation.Animation.INDEFINITE);
            rotacion.play();

            Map<String, String> filaRecetaCompleta = selector.getFilaRecetaSeleccionada();

            double costoDirecto = ParseUtils.toDouble(formExtra.getCostoDirecto());
            double ganancia = totalNum - costoDirecto;
            double costoUnitario = ParseUtils.toDouble(formExtra.getCostoUnitario());

            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("Código receta", codReceta);
            fila.put("Fecha", fecha);
            fila.put("Producto", nombreProducto);
            fila.put("Cantidad producida", cantidad);
            fila.put("Precio registrado", precioRegistrado);
            fila.put("Precio de Venta por Unidad", precioUnitario);
            fila.put("Mezcla", mezcla);
            fila.put("Costo directo", String.format("%.2f", costoDirecto));
            fila.put("Costo/U", String.format("%.4f", costoUnitario));
            fila.put("Costo Total", String.format("%.2f", costoDirecto));
            fila.put("Ganancia Total", String.format("%.2f", ganancia));

            if (filaRecetaCompleta != null) {
                fila.put("Unidades por Molde", filaRecetaCompleta.getOrDefault("Unidades por Molde", ""));
                fila.put("Molde/Paquete", filaRecetaCompleta.getOrDefault("Molde/Paquete", ""));
            }

            System.out.println("🔧 Precio unitario antes de guardar: " + precioUnitario);
            GuardarProduccionUtils.guardar(fila, formExtra.isGuardarReceta());

            rotacion.stop();
            btnGuardar.setRotate(0);
            btnGuardar.setDisable(false);
            btnGuardar.setText("✅ GUARDADO");
            btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");

            javafx.animation.ScaleTransition pulso = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), btnGuardar);
            pulso.setToX(1.1);
            pulso.setToY(1.1);
            pulso.setAutoReverse(true);
            pulso.setCycleCount(2);
            pulso.play();

            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), event -> {
                    btnGuardar.setText(textoOriginal);
                    btnGuardar.setStyle(estiloOriginal);
                })
            );
            timeline.play();
        });

        selector.setOnRecetaSeleccionada(filaCompleta -> {
            String codReceta = filaCompleta.getOrDefault("Código receta", null);
            String version = filaCompleta.getOrDefault("Versión", "-");
            String rendimiento = filaCompleta.getOrDefault("Rendimiento", "-");
            String nombreProducto = filaCompleta.getOrDefault("Producto", codReceta);
            if (codReceta == null) return;

            formExtra.setCodigoReceta(codReceta, filaCompleta);
            Node nodoFormulario = formExtra.crear(nombreProducto, version, rendimiento);
            formExtra.setCantidad("0");
            formExtra.setPrecioUnitario("0");
            formExtra.setPrecioTotal("0");
            formExtra.setMezclas("0");

            panelFormularioReceta.getChildren().setAll(nodoFormulario);

            Runnable actualizarIngredientes = () -> {
                double mezclas = ParseUtils.toDouble(formExtra.getMezclas());
                Map<String, String> produccionMock = new HashMap<>();
                produccionMock.put("Código receta", codReceta);
                produccionMock.put("Cantidad producida", formExtra.getCantidad());
                produccionMock.put("Costo directo", formExtra.getCostoDirecto());
                produccionMock.put("Costo/U", formExtra.getCostoUnitario());
                produccionMock.put("Rendimiento", rendimiento);
                produccionMock.put("Mezclas", String.valueOf(mezclas));

                VBox panelIngredientes = PanelIngredientesRecetaConMezclas.crear(
                        codReceta,
                        produccionMock,
                        "PRODUCCION_TEMPORAL",
                        mezclas,
                        (codigo, costo) -> {
                            formExtra.setCostoDirecto(String.format("%.2f", costo));
                            double cantidadProd = ParseUtils.toDouble(formExtra.getCantidad());
                            formExtra.setCostoUnitario(cantidadProd > 0 ? String.format("%.4f", costo / cantidadProd) : "0.0000");
                        }
                );
                panelIngredientesReceta.getChildren().setAll(panelIngredientes);
            };

            formExtra.setOnCambioMezclas(actualizarIngredientes);
            actualizarIngredientes.run();
        });

        contenedor.getChildren().addAll(titulo, detalle, filaBotones);
        return contenedor;
    }

    private static void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static void mostrarConfirmacion(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
