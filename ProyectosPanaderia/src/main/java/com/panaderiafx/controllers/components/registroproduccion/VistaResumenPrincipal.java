package com.panaderiafx.controllers.components.registroproduccion;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import com.panaderiafx.utils.VistaResumenUtils;
import com.panaderiafx.utils.cache.*;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.componentes.ResumenGananciasUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class VistaResumenPrincipal {

    public static Node crearVista(VBox panelDetalleProducciones, VBox panelDetalleReceta, VBox panelIngredientes) {
        VBox contenedorIzquierda = new VBox(15);
        contenedorIzquierda.setStyle("-fx-background-color: #FFECB3;");
        contenedorIzquierda.setPadding(new Insets(20));

        Label titulo = new Label("Resumen de Producción por Día");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        SelectorFechaTipo selector = new SelectorFechaTipo();

        VBox panelResumen = new VBox();
        panelResumen.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 10;");
        panelResumen.setSpacing(15);
        panelResumen.setPadding(new Insets(20));
        panelResumen.setMaxWidth(400);

        AtomicReference<PanelResumenProduccion> panelRef = new AtomicReference<>();

        // Botón guardar ganancias
        Button botonGuardarGanancias = new Button("💾 GUARDAR GANANCIAS");
        botonGuardarGanancias.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        botonGuardarGanancias.setOnAction(ev -> {
            TableView<Map<String, String>> tabla = (TableView<Map<String, String>>) panelDetalleProducciones.lookup(".table-view");
            if (tabla != null) {
                String fecha = selector.getFecha();
                for (Map<String, String> fila : tabla.getItems()) {
                    ResumenGananciasUtils.registrarGananciaProduccion(fecha, fila);
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText(null);
                alert.setContentText("✅ Ganancias guardadas correctamente.");
                alert.showAndWait();
            }
        });

        Runnable actualizar = () -> {
            String fecha = selector.getFecha();
            String tipo = selector.getTipo();

            VistaResumenUtils.calcularResumen(fecha, tipo);

            double ganancia = CacheGananciasUtils.get();
            double costoDirecto = CacheCostosDirectosUtils.get();
            double costoIndirecto = CacheCostosIndirectosUtils.get();
            double parametros = CacheParametrosUtils.get();
            double total = ganancia - costoDirecto - costoIndirecto - parametros;

            panelResumen.getChildren().clear();

            PanelResumenProduccion panel = new PanelResumenProduccion(
                ganancia, costoDirecto, costoIndirecto, parametros, total,
                (accion) -> {
                    panelDetalleReceta.setVisible(false);
                    panelIngredientes.setVisible(false);
                    panelDetalleReceta.getChildren().clear();
                    panelIngredientes.getChildren().clear();

                    switch (accion) {
                        case "GANANCIA_B.", "COSTOS_DIRECTOS_R." -> {
                            panelDetalleProducciones.getChildren().setAll(
                                VistaGananciasCostosDirectos.crear(
                                    fecha,
                                    tipo,
                                    (codigoReceta, fila) -> {
                                        panelDetalleReceta.setVisible(true);
                                        panelIngredientes.setVisible(true);

                                        Runnable actualizarIngredientes = () -> {
                                            panelIngredientes.getChildren().setAll(
                                                PanelIngredientesReceta.crear(codigoReceta, fila, (cod, nuevoCosto) -> {
                                                    TableView<Map<String, String>> tabla = (TableView<Map<String, String>>) panelDetalleProducciones.lookup(".table-view");
                                                    if (tabla != null) {
                                                        for (Map<String, String> item : tabla.getItems()) {
                                                            String codigo = item.getOrDefault("Código receta", "").trim();
                                                            if (codigo.equalsIgnoreCase(cod.trim())) {
                                                                double cantidad = ParseUtils.toDouble(item.getOrDefault("Cantidad producida", "1"));
                                                                double unitario = cantidad > 0 ? nuevoCosto / cantidad : 0;
                                                                item.put("Costo directo", String.format("%.2f", nuevoCosto));
                                                                item.put("Costo/U", String.format("%.2f", unitario));
                                                                break;
                                                            }
                                                        }
                                                        tabla.refresh();
                                                        TablaProduccionesFactory.recalcular(tabla.getItems(), (gan, cos) -> {
                                                            CacheGananciasUtils.set(gan);
                                                            CacheCostosDirectosUtils.set(cos);
                                                            panelRef.get().actualizarGananciaYCosto(gan, cos);
                                                        });
                                                    }
                                                })
                                            );
                                        };

                                        panelDetalleReceta.getChildren().setAll(
                                            PanelFormularioReceta.crear(codigoReceta, fila, (cod, nuevoTotal) -> {
                                                TableView<Map<String, String>> tabla = (TableView<Map<String, String>>) panelDetalleProducciones.lookup(".table-view");
                                                if (tabla != null) {
                                                    for (Map<String, String> item : tabla.getItems()) {
                                                        String codigo = item.getOrDefault("Código receta", "").trim();
                                                        if (codigo.equalsIgnoreCase(cod.trim())) {
                                                            double cantidad = ParseUtils.toDouble(item.getOrDefault("Cantidad producida", "1"));
                                                            double nuevoUnitario = Math.floor((nuevoTotal / cantidad) * 100) / 100;
                                                            item.put("Precio de Venta por Unidad", String.format("%.2f", nuevoUnitario));
                                                            item.put("Cantidad producida", String.format("%.0f", cantidad));
                                                            break;
                                                        }
                                                    }
                                                    tabla.refresh();
                                                    TablaProduccionesFactory.recalcular(tabla.getItems(), (gan, cos) -> {
                                                        CacheGananciasUtils.set(gan);
                                                        CacheCostosDirectosUtils.set(cos);
                                                        panelRef.get().actualizarGananciaYCosto(gan, cos);
                                                    });
                                                }
                                            }, actualizarIngredientes)
                                        );

                                        actualizarIngredientes.run();
                                    },
                                    (gan, cos) -> {
                                        CacheGananciasUtils.set(gan);
                                        CacheCostosDirectosUtils.set(cos);
                                        panelRef.get().actualizarGananciaYCosto(gan, cos);
                                    }
                                )
                            );

                            PanelIngredientesReceta.forzarRecalculo();
                        }
                        case "COSTOS_INDIRECTOS" -> {
                            panelDetalleProducciones.getChildren().setAll(
                                PanelCostosIndirectosResumen.crear()
                            );
                        }
                        case "PARÁMETROS" -> {
                            panelDetalleProducciones.getChildren().setAll(
                                PanelParametrosResumen.crear()
                            );
                        }
                    }
                }
            );

            panelRef.set(panel);
            panelResumen.getChildren().addAll(panel, botonGuardarGanancias);

            CacheParametrosUtils.agregarObservador(() -> {
                double nuevo = CacheParametrosUtils.get();
                if (panelRef.get() != null) {
                    panelRef.get().actualizarParametros(nuevo);
                }
            });
        };

        selector.getBotonActualizar().setOnAction(e -> actualizar.run());
        selector.getBotonSeleccionar().setOnAction(e -> actualizar.run());

        actualizar.run();
        contenedorIzquierda.getChildren().addAll(titulo, selector, panelResumen);
        return contenedorIzquierda;
    }
}
