package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import com.panaderiafx.utils.VistaResumenUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class VistaResumenPrincipal {

    public static Node crearVista(VBox panelDetalleProducciones, VBox panelDetalleReceta, VBox panelIngredientes) {
        VBox contenedorIzquierda = new VBox(15);
        contenedorIzquierda.setStyle("-fx-background-color: #FFECB3;");
        contenedorIzquierda.setPadding(new Insets(20));

        Label titulo = new Label("Resumen de Producción por Día o Mes");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        SelectorFechaTipo selector = new SelectorFechaTipo();

        VBox panelResumen = new VBox();
        panelResumen.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");
        panelResumen.setSpacing(15);
        panelResumen.setPadding(new Insets(20));
        panelResumen.setMaxWidth(400);

        AtomicReference<PanelResumenProduccion> panelRef = new AtomicReference<>();

        Runnable actualizar = () -> {
            String fecha = selector.getFecha();
            String tipo = selector.getTipo();
            System.out.println("🔍 Recalculando vista con fecha: " + fecha + " y tipo: " + tipo);

            Map<String, Double> resumen = VistaResumenUtils.calcularResumen(fecha, tipo);

            panelResumen.getChildren().clear();

            PanelResumenProduccion panel = new PanelResumenProduccion(
                resumen.getOrDefault("ganancias", 0.0),
                resumen.getOrDefault("costos_directos", 0.0),
                resumen.getOrDefault("costos_indirectos", 0.0),
                resumen.getOrDefault("parametros", 0.0),
                resumen.getOrDefault("total", 0.0),
                (accion) -> {
                    System.out.println("🟠 Acción clickeada: " + accion);
                    if (accion.equals("GANANCIAS") || accion.equals("COSTOS_DIRECTOS")) {
                        panelDetalleProducciones.getChildren().setAll(
                            VistaGananciasCostosDirectos.crear(
                                fecha,
                                tipo,
                                (codigoReceta) -> {
                                    panelDetalleReceta.getChildren().setAll(PanelFormularioReceta.crear(codigoReceta));
                                    panelIngredientes.getChildren().setAll(PanelIngredientesReceta.crear(codigoReceta));
                                },
                                (ganancia, costo) -> {
                                    if (panelRef.get() != null) {
                                        panelRef.get().actualizarGananciaYCosto(ganancia, costo);
                                    }
                                }
                            )
                        );
                    } else {
                        panelDetalleProducciones.getChildren().clear();
                        panelDetalleReceta.getChildren().clear();
                        panelIngredientes.getChildren().clear();
                    }
                }
            );

            panelRef.set(panel);
            panelResumen.getChildren().add(panel);
        };

        selector.getBotonActualizar().setOnAction(e -> actualizar.run());
        selector.getBotonSeleccionar().setOnAction(e -> actualizar.run());

        actualizar.run();

        contenedorIzquierda.getChildren().addAll(titulo, selector, panelResumen);
        return contenedorIzquierda;
    }
}
