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
            VerUtils.refrescarExcel(); // 🔄 recarga todo el Excel en memoria
            selector.recargar();       // 🔁 actualiza tabla de recetas
        });

        HBox filaBotones = new HBox(10, btnGuardar, btnActualizar);

        btnGuardar.setOnAction(e -> {
            String codReceta = selector.getCodigoRecetaSeleccionado();
            String fecha = selector.getFechaSeleccionada();
            String nombreProducto = VerUtils.buscarPorCodigo("Recetas", "Código receta", codReceta, "Producto");
            String cantidad = formExtra.getCantidad();
            String precioRegistrado = formExtra.getPrecioRegistrado();
            String precioUnitario = formExtra.getPrecioUnitario(); // 🔧 OBTENER PRECIO UNITARIO
            String total = formExtra.getPrecioTotal();
            String mezcla = formExtra.getMezclas();

            if (codReceta == null || fecha.isEmpty() || cantidad.isEmpty() || precioRegistrado.isEmpty() || total.isEmpty()) {
                mostrarError("Complete todos los campos antes de guardar.");
                return;
            }

            try {
                if (Double.parseDouble(cantidad) == 0 || Double.parseDouble(precioRegistrado) == 0 || Double.parseDouble(total) == 0) {
                    mostrarError("Cantidad, precio registrado y total deben ser mayores a cero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarError("Los valores numéricos no son válidos.");
                return;
            }

            // 🎬 ANIMACIÓN DE CARGA CON ROTACIÓN
            String textoOriginal = btnGuardar.getText();
            String estiloOriginal = btnGuardar.getStyle();
            
            btnGuardar.setDisable(true);
            btnGuardar.setText("⏳ GUARDANDO...");
            btnGuardar.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
            
            // 🌀 ANIMACIÓN ROTATIVA MIENTRAS GUARDA
            javafx.animation.RotateTransition rotacion = new javafx.animation.RotateTransition(javafx.util.Duration.millis(1000), btnGuardar);
            rotacion.setByAngle(360);
            rotacion.setCycleCount(javafx.animation.Animation.INDEFINITE);
            rotacion.play();

            // Obtener la fila completa de la receta para tener todos los datos necesarios
            Map<String, String> filaRecetaCompleta = selector.getFilaRecetaSeleccionada();
            
            // 🔧 Crear el Map con todos los datos necesarios para el cálculo correcto
            double totalNum = ParseUtils.toDouble(total);
            double costoDirecto = ParseUtils.toDouble(formExtra.getCostoDirecto());
            double ganancia = totalNum - costoDirecto;
            double costoUnitario = ParseUtils.toDouble(formExtra.getCostoUnitario());

            Map<String, String> fila = new LinkedHashMap<>();
            fila.put("Código receta", codReceta);
            fila.put("Fecha", fecha);
            fila.put("Producto", nombreProducto);
            fila.put("Cantidad producida", cantidad);
            fila.put("Precio registrado", precioRegistrado); // 🔧 Precio registrado original
            fila.put("Precio de Venta por Unidad", precioUnitario); // 🔧 PRECIO UNITARIO CALCULADO
            fila.put("Mezcla", mezcla);
            fila.put("Costo directo", String.format("%.2f", costoDirecto));
            fila.put("Costo/U", String.format("%.4f", costoUnitario));
            fila.put("Costo Total", String.format("%.2f", costoDirecto));
            fila.put("Ganancia Total", String.format("%.2f", ganancia));
            
            // 🔧 Agregar datos necesarios para el cálculo del precio por unidad
            if (filaRecetaCompleta != null) {
                fila.put("Unidades por Molde", filaRecetaCompleta.getOrDefault("Unidades por Molde", ""));
                fila.put("Molde/Paquete", filaRecetaCompleta.getOrDefault("Molde/Paquete", ""));
            }

            // 🔧 GUARDADO DIRECTO EN EL HILO DE JAVAFX CON ANIMACIÓN
            try {
                System.out.println("🔧 Precio unitario antes de guardar: " + precioUnitario);
                GuardarProduccionUtils.guardar(fila, formExtra.isGuardarReceta());
                
                // 🛑 PARAR ANIMACIÓN DE CARGA
                rotacion.stop();
                btnGuardar.setRotate(0); // Resetear rotación
                
                // 🎉 ÉXITO - ANIMACIÓN RESTAURADA
                btnGuardar.setDisable(false);
                btnGuardar.setText("✅ GUARDADO");
                btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
                
                // 🎬 ANIMACIÓN: Pulso de éxito
                javafx.animation.ScaleTransition pulso = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), btnGuardar);
                pulso.setToX(1.1);
                pulso.setToY(1.1);
                pulso.setAutoReverse(true);
                pulso.setCycleCount(2);
                pulso.play();
                
                // 🔄 Volver al estado original después de 2 segundos
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), event -> {
                        btnGuardar.setText(textoOriginal);
                        btnGuardar.setStyle(estiloOriginal);
                    })
                );
                timeline.play();
                
            } catch (Exception ex) {
                // 🛑 PARAR ANIMACIÓN DE CARGA EN ERROR
                rotacion.stop();
                btnGuardar.setRotate(0); // Resetear rotación
                
                // ❌ ERROR - ANIMACIÓN DE ERROR
                btnGuardar.setDisable(false);
                btnGuardar.setText("❌ ERROR");
                btnGuardar.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20;");
                
                // 🎬 ANIMACIÓN: Shake de error
                javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(100), btnGuardar);
                shake.setFromX(0);
                shake.setToX(10);
                shake.setAutoReverse(true);
                shake.setCycleCount(6);
                shake.play();
                
                System.err.println("❌ Error al guardar: " + ex.getMessage());
                ex.printStackTrace();
                mostrarError("Error al guardar: " + ex.getMessage());
                
                // 🔄 Volver al estado original después de 3 segundos
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), event -> {
                        btnGuardar.setText(textoOriginal);
                        btnGuardar.setStyle(estiloOriginal);
                    })
                );
                timeline.play();
            }
        });

        selector.setOnRecetaSeleccionada(filaCompleta -> {
            String codReceta = filaCompleta.getOrDefault("Código receta", null);
            String version = filaCompleta.getOrDefault("Versión", "-");
            String rendimiento = filaCompleta.getOrDefault("Rendimiento", "-");
            String nombreProducto = filaCompleta.getOrDefault("Producto", codReceta);
            if (codReceta == null) return;

            formExtra.setCodigoReceta(codReceta, filaCompleta); // 🟢 PASAMOS LA FILA COMPLETA
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