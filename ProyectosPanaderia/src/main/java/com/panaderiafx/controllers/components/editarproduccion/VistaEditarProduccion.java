package com.panaderiafx.controllers.components.editarproduccion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

public class VistaEditarProduccion extends BorderPane {

    private final VBox panelFormulario;
    private final VBox panelIngredientes;

    public VistaEditarProduccion() {
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #FFF3E0;");

        HBox contenedor = new HBox(30);
        contenedor.setPadding(new Insets(10));
        contenedor.setStyle("-fx-background-color: #FFE0B2; -fx-background-radius: 10;");
        contenedor.setPrefHeight(1100);

        panelFormulario = crearPlaceholder("Formulario editable de producción");
        panelFormulario.setPrefWidth(600);

        panelIngredientes = crearPlaceholder("Tabla editable de ingredientes usados");
        panelIngredientes.setPrefWidth(700);

        Node selector = SelectorProduccionEditorFactory.crearSelector((produccion) -> {
            if (produccion == null) return;

            // Lista compartida entre tabla y formulario
            ObservableList<Map<String, String>> datosIngredientes = FXCollections.observableArrayList();

            // Crear formulario editable vinculado a lista
            VBox formulario = FormularioEditorProduccionFactory.crearFormulario(produccion, datosIngredientes);

            // Callback para actualizar totales (costo total)
            BiConsumer<String, Double> actualizarTotales = (codigo, nuevoCostoTotal) -> {
                FormularioEditorProduccionFactory.actualizarTotales(codigo, nuevoCostoTotal);
            };

            // Crear panel de ingredientes dinámicamente escalado
            VBox ingredientes = PanelIngredientesEditorFactory.crear(
                    produccion,
                    datosIngredientes,
                    actualizarTotales,
                    Collections.emptyMap() // Puedes pasar mapaNombreIngredientes real si lo tienes
            );

            // Actualizar paneles visuales
            panelFormulario.getChildren().setAll(formulario.getChildren());
            panelIngredientes.getChildren().setAll(ingredientes.getChildren());
        });

        VBox contenedorSelector = new VBox(selector);
        contenedorSelector.setPrefWidth(600);

        contenedor.getChildren().addAll(contenedorSelector, panelFormulario, panelIngredientes);
        setCenter(contenedor);
    }

    private VBox crearPlaceholder(String texto) {
        VBox box = new VBox();
        box.setStyle("-fx-background-color: #FFE082; -fx-padding: 20; -fx-background-radius: 10;");
        box.getChildren().add(new Label("🛠️ " + texto));
        return box;
    }
}
