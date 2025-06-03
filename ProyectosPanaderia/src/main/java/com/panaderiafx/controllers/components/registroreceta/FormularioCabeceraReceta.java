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
    // Nuevos campos de precio
    private final TextField campoPrecioMayor = new TextField();
    private final TextField campoPrecioPublico = new TextField();

    private final GridPane grid = new GridPane();

    public FormularioCabeceraReceta() {
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #F36C00; -fx-background-radius: 10;");

        comboProducto.setEditable(true);
        comboProducto.setItems(FXCollections.observableArrayList(obtenerProductos()));
        comboProducto.setOnAction(e -> autocompletarVersion());
        
        // También actualizar versión cuando se edita el texto del producto
        comboProducto.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.trim().isEmpty()) {
                autocompletarVersion();
            }
        });

        campoCodigo.setEditable(false);
        campoVersion.setEditable(false);
        campoCodigo.setText(CodigoGenerator.generarCodigo("Recetas", "Código receta"));

        comboUnidad.setItems(FXCollections.observableArrayList("Unidades", "Gramos", "Libras", "Litros", "Onzas"));
        comboCategoria.setItems(FXCollections.observableArrayList("Pan", "Repostería"));
        comboSubcategoria.setItems(FXCollections.observableArrayList("Blanco", "Dulce", "Salado", "Especial", "Integral", "Refinado", "Mezcla", "Galletas"));

        campoObservaciones.setPrefRowCount(3);

        // Configurar validaciones numéricas
        restringirSoloNumeros(campoRendimiento);
        restringirSoloNumeros(campoUnidadesLote);
        restringirSoloNumeros(campoMoldeLote);
        restringirSoloNumerosDecimales(campoPrecioMayor);
        restringirSoloNumerosDecimales(campoPrecioPublico);

        // Configurar placeholders para los campos de precio
        campoPrecioMayor.setPromptText("Opcional - Ej: 1.85");
        campoPrecioPublico.setPromptText("Opcional - Ej: 2.35");

        // Layout del formulario
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

        grid.add(crearEtiqueta("Unidades por Molde:"), 0, f);
        grid.add(campoUnidadesLote, 1, f++);

        grid.add(crearEtiqueta("Molde/Paquete:"), 0, f);
        grid.add(campoMoldeLote, 1, f++);

        grid.add(crearEtiqueta("Categoría:"), 0, f);
        grid.add(comboCategoria, 1, f++);

        grid.add(crearEtiqueta("Subcategoría:"), 0, f);
        grid.add(comboSubcategoria, 1, f++);

        // Nuevos campos de precio
        grid.add(crearEtiqueta("Precio por Mayor (Opcional):"), 0, f);
        grid.add(campoPrecioMayor, 1, f++);

        grid.add(crearEtiqueta("Precio Público Supermarket (Opcional):"), 0, f);
        grid.add(campoPrecioPublico, 1, f++);

        grid.add(crearEtiqueta("Observaciones:"), 0, f);
        grid.add(campoObservaciones, 1, f++);
    }

    private void restringirSoloNumeros(TextField campo) {
        campo.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if (!e.getCharacter().matches("[0-9.]")) e.consume();
        });
    }

    private void restringirSoloNumerosDecimales(TextField campo) {
        campo.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String caracter = e.getCharacter();
            String textoActual = campo.getText();
            
            // Permitir números, punto decimal y borrar
            if (!caracter.matches("[0-9.]")) {
                e.consume();
                return;
            }
            
            // Permitir solo un punto decimal
            if (caracter.equals(".") && textoActual.contains(".")) {
                e.consume();
                return;
            }
            
            // Limitar a 2 decimales
            if (textoActual.contains(".")) {
                String[] partes = textoActual.split("\\.");
                if (partes.length > 1 && partes[1].length() >= 2) {
                    e.consume();
                }
            }
        });
    }

    private void autocompletarVersion() {
        String producto = comboProducto.getEditor().getText().trim();
        if (!producto.isEmpty()) {
            String nuevaVersion = VersionUtils.getNuevaVersion(producto);
            campoVersion.setText(nuevaVersion);
        } else {
            campoVersion.clear(); // Limpiar versión si no hay producto
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
        datos.put("Código receta", campoCodigo.getText().trim());
        datos.put("Producto", comboProducto.getEditor().getText().trim());
        datos.put("Rendimiento", campoRendimiento.getText().trim());
        datos.put("Unidad Rendimiento", comboUnidad.getValue());
        datos.put("Unidades por Molde", campoUnidadesLote.getText().trim());
        datos.put("Molde/Paquete", campoMoldeLote.getText().trim());
        datos.put("Categoría", comboCategoria.getValue());
        datos.put("Subcategoría", comboSubcategoria.getValue());
        datos.put("Observaciones", campoObservaciones.getText().trim().isEmpty() ? "Ninguna" : campoObservaciones.getText().trim());
        datos.put("Versión", campoVersion.getText().trim());
        
        // Nuevos campos de precio - validar que no estén vacíos
        String precioMayor = campoPrecioMayor.getText().trim();
        String precioPublico = campoPrecioPublico.getText().trim();
        
        datos.put("Precio por Mayor", precioMayor.isEmpty() ? "" : precioMayor);
        datos.put("Precio Publics Supermarket", precioPublico.isEmpty() ? "" : precioPublico);
        
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
        campoPrecioMayor.clear();
        campoPrecioPublico.clear();
        campoCodigo.setText(CodigoGenerator.generarCodigo("Recetas", "Código receta"));
    }

    // Métodos adicionales para validación
    public boolean validarCamposObligatorios() {
        List<String> errores = new ArrayList<>();
        
        if (comboProducto.getEditor().getText().trim().isEmpty()) {
            errores.add("Producto es obligatorio");
        }
        if (campoRendimiento.getText().trim().isEmpty()) {
            errores.add("Rendimiento es obligatorio");
        }
        if (comboUnidad.getValue() == null) {
            errores.add("Unidad de Rendimiento es obligatoria");
        }
        if (comboCategoria.getValue() == null) {
            errores.add("Categoría es obligatoria");
        }
        if (comboSubcategoria.getValue() == null) {
            errores.add("Subcategoría es obligatoria");
        }
        
        if (!errores.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos Obligatorios");
            alert.setHeaderText("Faltan campos por completar:");
            alert.setContentText(String.join("\n", errores));
            alert.showAndWait();
            return false;
        }
        
        return true;
    }

    public boolean validarFormatoPrecios() {
        String precioMayor = campoPrecioMayor.getText().trim();
        String precioPublico = campoPrecioPublico.getText().trim();
        
        if (!precioMayor.isEmpty()) {
            try {
                double precio = Double.parseDouble(precioMayor);
                if (precio < 0) {
                    mostrarErrorPrecio("El precio por mayor no puede ser negativo");
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarErrorPrecio("Formato inválido en precio por mayor");
                return false;
            }
        }
        
        if (!precioPublico.isEmpty()) {
            try {
                double precio = Double.parseDouble(precioPublico);
                if (precio < 0) {
                    mostrarErrorPrecio("El precio público no puede ser negativo");
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarErrorPrecio("Formato inválido en precio público");
                return false;
            }
        }
        
        return true;
    }

    private void mostrarErrorPrecio(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en Precio");
        alert.setHeaderText("Error de validación:");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Getters para acceso individual a los campos
    public String getPrecioMayor() {
        return campoPrecioMayor.getText().trim();
    }

    public String getPrecioPublico() {
        return campoPrecioPublico.getText().trim();
    }

    public void setPrecioMayor(String precio) {
        campoPrecioMayor.setText(precio);
    }

    public void setPrecioPublico(String precio) {
        campoPrecioPublico.setText(precio);
    }
}