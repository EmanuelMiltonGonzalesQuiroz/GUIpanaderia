package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.panaderiafx.utils.VistaResumenUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class VistaResumenPrincipal {

    public static Node crearVista() {
        HBox contenedor = new HBox(30);
        contenedor.setStyle("-fx-background-color: #FFECB3; -fx-padding: 20;");

        VBox seccionResumen = new VBox(15);
        seccionResumen.setStyle("-fx-background-color: #FFECB3;");
        Label titulo = new Label("Resumen de Producción por Día o Mes");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        seccionResumen.getChildren().add(titulo);

        SelectorFechaTipo selector = new SelectorFechaTipo();

        VBox panelResumen = new VBox();
        panelResumen.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");
        panelResumen.setSpacing(15);
        panelResumen.setPadding(new javafx.geometry.Insets(20));
        panelResumen.setMaxWidth(400);

        VBox panelDetalle = new VBox();
        panelDetalle.setStyle("-fx-background-color: #FFE0B2; -fx-padding: 20; -fx-background-radius: 10;");
        panelDetalle.setSpacing(10);
        panelDetalle.setPrefWidth(450);

        AtomicReference<PanelResumenProduccion> panelRef = new AtomicReference<>();

        Runnable actualizar = () -> {
            String fecha = selector.getFecha();
            String tipo = selector.getTipo();
            Map<String, Double> resumen = VistaResumenUtils.calcularResumen(fecha, tipo);

            panelResumen.getChildren().clear();

            PanelResumenProduccion panel = new PanelResumenProduccion(
                resumen.getOrDefault("ganancias", 0.0),
                resumen.getOrDefault("costos_directos", 0.0),
                resumen.getOrDefault("costos_indirectos", 0.0),
                resumen.getOrDefault("parametros", 0.0),
                resumen.getOrDefault("total", 0.0),
                (accion) -> {
                    if (accion.equals("GANANCIAS") || accion.equals("COSTOS_DIRECTOS")) {
                        PanelDetalleProducciones.mostrar(panelDetalle, fecha, tipo, panelRef.get()::actualizarGananciaYCostos);
                    } else {
                        panelDetalle.getChildren().clear();
                    }
                }
            );

            panelRef.set(panel);
            panelResumen.getChildren().add(panel);
        };

        selector.getBotonActualizar().setOnAction(e -> actualizar.run());
        selector.getBotonSeleccionar().setOnAction(e -> actualizar.run());

        actualizar.run();

        seccionResumen.getChildren().addAll(selector, panelResumen);
        contenedor.getChildren().addAll(seccionResumen, panelDetalle);
        return contenedor;
    }
}
