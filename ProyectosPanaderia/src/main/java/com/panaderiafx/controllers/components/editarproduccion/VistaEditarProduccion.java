package com.panaderiafx.controllers.components.editarproduccion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.function.BiConsumer;

public class VistaEditarProduccion extends BorderPane {

    private final VBox panelFormulario;
    private final VBox panelIngredientes;
    private final VBox contenedorSelector;

    private ObservableList<Map<String, String>> datosIngredientes;
    private Map<String, String> produccionActual;

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

        Button botonActualizar = new Button("🔄 Actualizar Vista");
        botonActualizar.setStyle("-fx-font-size: 14px; -fx-background-color: #FFB74D; -fx-text-fill: black;");
        botonActualizar.setOnAction(e -> recargarVistaCompleta());

        Node selector = SelectorProduccionEditorFactory.crearSelector((produccion) -> {
            if (produccion == null) return;

            this.produccionActual = produccion;
            this.datosIngredientes = FXCollections.observableArrayList();

            recargarVistaCompleta();
        });

        contenedorSelector = new VBox(10, botonActualizar, selector);
        contenedorSelector.setPrefWidth(600);

        contenedor.getChildren().addAll(contenedorSelector, panelFormulario, panelIngredientes);
        setCenter(contenedor);
    }

    private void recargarVistaCompleta() {
        if (this.produccionActual == null || this.datosIngredientes == null) return;

        BiConsumer<String, Double> actualizarTotales = (codigo, nuevoCostoTotal) -> {
            FormularioEditorProduccionFactory.actualizarTotales(codigo, nuevoCostoTotal);
            recargarTablaIngredientes();
        };

        VBox formulario = FormularioEditorProduccionFactory.crearFormulario(
                produccionActual, datosIngredientes, this::recargarTablaIngredientes
        );

        VBox ingredientes = PanelIngredientesEditorFactory.crear(
                produccionActual, datosIngredientes, actualizarTotales
        );

        panelFormulario.getChildren().setAll(formulario.getChildren());
        panelIngredientes.getChildren().setAll(ingredientes.getChildren());
    }

    private void recargarTablaIngredientes() {
        if (this.produccionActual == null || this.datosIngredientes == null) return;

        BiConsumer<String, Double> actualizarTotales = (codigo, nuevoCostoTotal) -> {
            FormularioEditorProduccionFactory.actualizarTotales(codigo, nuevoCostoTotal);
        };

        VBox ingredientesActualizados = PanelIngredientesEditorFactory.crear(
                produccionActual, datosIngredientes, actualizarTotales
        );

        panelIngredientes.getChildren().setAll(ingredientesActualizados.getChildren());
    }

    private VBox crearPlaceholder(String texto) {
        VBox box = new VBox();
        box.setStyle("-fx-background-color: #FFE082; -fx-padding: 20; -fx-background-radius: 10;");
        box.getChildren().add(new Label("🛠️ " + texto));
        return box;
    }
}
