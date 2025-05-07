package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import com.panaderiafx.utils.VistaResumenUtils;
import com.panaderiafx.utils.cache.*;

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

            VistaResumenUtils.calcularResumen(fecha, tipo);

            double ganancia = CacheGananciasUtils.get();
            double costoDirecto = CacheCostosDirectosUtils.total();
            double costoIndirecto = CacheCostosIndirectosUtils.get();
            double parametros = CacheParametrosUtils.get();
            double total = ganancia - costoDirecto - costoIndirecto - parametros;

            panelResumen.getChildren().clear();

            PanelResumenProduccion panel = new PanelResumenProduccion(
                ganancia, costoDirecto, costoIndirecto, parametros, total,
                (accion) -> {
                    if (accion.equals("GANANCIAS") || accion.equals("COSTOS_DIRECTOS")) {
                        panelDetalleProducciones.getChildren().setAll(
                            VistaGananciasCostosDirectos.crear(
                                fecha,
                                tipo,
                                (codigoReceta, fila) -> {
                                    panelDetalleReceta.setVisible(true);
                                    panelIngredientes.setVisible(true);
                                    panelDetalleReceta.getChildren().setAll(
                                        PanelFormularioReceta.crear(codigoReceta, fila, (cod, nuevoTotal) -> {                            
                                            TableView<Map<String, String>> tabla = (TableView<Map<String, String>>) panelDetalleProducciones.lookup(".table-view");
                                            if (tabla != null) {
                                                for (Map<String, String> item : tabla.getItems()) {
                                                    String codigo = item.getOrDefault("Código receta", "").trim();
                                                    if (codigo.equalsIgnoreCase(cod.trim())) {
                                                        double cantidad = Double.parseDouble(item.getOrDefault("Cantidad producida", "1"));
                                                        double nuevoUnitario = Math.floor((nuevoTotal / cantidad) * 100) / 100;
                                                        item.put("Precio de Venta por Unidad", String.format("%.2f", nuevoUnitario));
                                                        break;
                                                    }
                                                }
                                                tabla.refresh();

                                                TablaProduccionesFactory.recalcular(tabla.getItems(), (gan, cos) -> {
                                                    CacheGananciasUtils.set(gan);
                                                    CacheCostosDirectosUtils.setTotal(cos);
                                                    panelRef.get().actualizarGananciaYCosto(gan, cos);
                                                });
                                            }
                                        })
                                    );
                                    panelIngredientes.getChildren().setAll(PanelIngredientesReceta.crear(codigoReceta));
                                },
                                (gan, cos) -> {
                                    CacheGananciasUtils.set(gan);
                                    CacheCostosDirectosUtils.setTotal(cos);
                                    panelRef.get().actualizarGananciaYCosto(gan, cos);
                                }
                            )
                        );
                    } else {
                        panelDetalleProducciones.getChildren().clear();
                        panelDetalleReceta.getChildren().clear();
                        panelIngredientes.getChildren().clear();
                        panelDetalleReceta.setVisible(false);
                        panelIngredientes.setVisible(false);
                    }
                });

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
