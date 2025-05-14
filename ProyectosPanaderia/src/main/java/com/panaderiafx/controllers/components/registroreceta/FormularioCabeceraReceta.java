package com.panaderiafx.controllers.components.registroreceta;

import com.panaderiafx.utils.CodigoGenerator;
import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.VersionUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

import java.util.*;

public class FormularioCabeceraReceta {

    private final TextField campoCodigo = new TextField();
    private final TextField campoVersion = new TextField();
    private final ComboBox<String> comboProducto = new ComboBox<>();
    private final TextField campoRendimiento = new TextField();
    private final ComboBox<String> comboUnidad = new ComboBox<>();
    private final TextField campoUnidadesLote = new TextField();
    private final TextField campoMoldeLote = new TextField();
    private final ComboBox<String> comboCategoria = new ComboBox<>();
    private final ComboBox<String> comboSubcategoria = new ComboBox<>();
    private final TextArea campoObservaciones = new TextArea();

    private final GridPane grid = new GridPane();

    public FormularioCabeceraReceta() {
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #F36C00; -fx-background-radius: 10;");

        comboProducto.setEditable(true);
        comboProducto.setItems(FXCollections.observableArrayList(obtenerProductos()));
        comboProducto.setOnAction(e -> autocompletarVersion());

        campoCodigo.setEditable(false);
        campoVersion.setEditable(false);
        campoCodigo.setText(CodigoGenerator.generarCodigo("Recetas", "Código receta"));

        comboUnidad.setItems(FXCollections.observableArrayList("Unidades", "Gramos", "Libras", "Litros", "Onzas"));
        comboCategoria.setItems(FXCollections.observableArrayList("Pan", "Repostería"));
        comboSubcategoria.setItems(FXCollections.observableArrayList("Blanco", "Dulce", "Salado", "Especial", "Integral", "Refinado", "Mezcla", "Galletas"));

        campoObservaciones.setPrefRowCount(3);

        restringirSoloNumeros(campoRendimiento);
        restringirSoloNumeros(campoUnidadesLote);
        restringirSoloNumeros(campoMoldeLote);

        int f = 0;
        grid.add(crearEtiqueta("Producto:"), 0, f);
        grid.add(comboProducto, 1, f++);

        grid.add(crearEtiqueta("Código receta:"), 0, f);
        grid.add(campoCodigo, 1, f++);

        grid.add(crearEtiqueta("Versión:"), 0, f);
        grid.add(campoVersion, 1, f++);

        grid.add(crearEtiqueta("Rendimiento:"), 0, f);
        grid.add(campoRendimiento, 1, f++);

        grid.add(crearEtiqueta("Unidad Rendimiento:"), 0, f);
        grid.add(comboUnidad, 1, f++);

        grid.add(crearEtiqueta("Unidades por Lote:"), 0, f);
        grid.add(campoUnidadesLote, 1, f++);

        grid.add(crearEtiqueta("Molde/Lote:"), 0, f);
        grid.add(campoMoldeLote, 1, f++);

        grid.add(crearEtiqueta("Categoría:"), 0, f);
        grid.add(comboCategoria, 1, f++);

        grid.add(crearEtiqueta("Subcategoría:"), 0, f);
        grid.add(comboSubcategoria, 1, f++);

        grid.add(crearEtiqueta("Observaciones:"), 0, f);
        grid.add(campoObservaciones, 1, f++);
    }

    private void restringirSoloNumeros(TextField campo) {
        campo.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[0-9.]")) e.consume();
        });
    }

    private void autocompletarVersion() {
        String producto = comboProducto.getEditor().getText().trim();
        if (!producto.isEmpty()) {
            String nuevaVersion = VersionUtils.getNuevaVersion(producto);
            campoVersion.setText(nuevaVersion);
        }
    }

    private Label crearEtiqueta(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-background-color: #FFC107; -fx-font-weight: bold; -fx-padding: 5 10; -fx-font-size: 13px;");
        return lbl;
    }

    private List<String> obtenerProductos() {
        List<Map<String, String>> recetas = VerUtils.verTabla("Recetas");
        Set<String> productos = new TreeSet<>();
        for (Map<String, String> fila : recetas) {
            String prod = fila.getOrDefault("Producto", "").trim();
            if (!prod.isEmpty()) productos.add(prod);
        }
        return new ArrayList<>(productos);
    }

    public Node getNode() {
        return grid;
    }

    public Map<String, String> getDatos() {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("Producto", comboProducto.getEditor().getText().trim());
        datos.put("Código receta", campoCodigo.getText().trim());
        datos.put("Versión", campoVersion.getText().trim());
        datos.put("Rendimiento", campoRendimiento.getText().trim());
        datos.put("Unidad Rendimiento", comboUnidad.getValue());
        datos.put("Unidades por Lote", campoUnidadesLote.getText().trim());
        datos.put("Molde/Lote", campoMoldeLote.getText().trim());
        datos.put("Categoría", comboCategoria.getValue());
        datos.put("Subcategoría", comboSubcategoria.getValue());
        datos.put("Observaciones", campoObservaciones.getText().trim().isEmpty() ? "Ninguna" : campoObservaciones.getText().trim());
        return datos;
    }

    public void limpiarCampos() {
        comboProducto.getEditor().clear();
        campoVersion.clear();
        campoRendimiento.clear();
        comboUnidad.getSelectionModel().clearSelection();
        campoUnidadesLote.clear();
        campoMoldeLote.clear();
        comboCategoria.getSelectionModel().clearSelection();
        comboSubcategoria.getSelectionModel().clearSelection();
        campoObservaciones.clear();
        campoCodigo.setText(CodigoGenerator.generarCodigo("Recetas", "Código receta"));
    }
}
