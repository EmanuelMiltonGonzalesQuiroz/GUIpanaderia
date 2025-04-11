package com.panaderiafx.controllers.components;

import com.panaderiafx.utils.EliminarUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class FormularioEliminar extends ContenedorFlexible {

    private final String nombreTabla;
    private final Map<String, Node> campos = new LinkedHashMap<>();
    private final List<Map<String, String>> registros;
    private final List<String> columnas;

    public FormularioEliminar(String nombreTabla, List<String> columnas, List<Map<String, String>> registros) {
        this.nombreTabla = nombreTabla;
        this.columnas = columnas;
        this.registros = registros;

        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setMaxWidth(500);
        contenedor.setStyle("-fx-background-color: #FFF8E1;");

        ComboBox<String> selector = new ComboBox<>();
        for (Map<String, String> fila : registros) {
            String id = fila.getOrDefault("Código", fila.values().iterator().next());
            selector.getItems().add(id);
        }

        VBox formulario = new VBox(15);
        selector.setPromptText("Selecciona un registro a eliminar");

        selector.setOnAction(e -> {
            String seleccion = selector.getSelectionModel().getSelectedItem();
            Map<String, String> filaSeleccionada = registros.stream()
                    .filter(f -> f.containsValue(seleccion))
                    .findFirst().orElse(null);

            if (filaSeleccionada != null) {
                formulario.getChildren().clear();
                campos.clear();
                construirFormulario(filaSeleccionada, formulario);
                formulario.getChildren().add(crearBotonEliminar(filaSeleccionada));
            }
        });

        contenedor.getChildren().addAll(selector, formulario);
        this.setContenido(contenedor);
    }

    private void construirFormulario(Map<String, String> datos, VBox contenedor) {
        for (String nombre : columnas) {
            String valor = datos.getOrDefault(nombre, "");

            CampoTexto campoTexto = new CampoTexto("");
            campoTexto.setText(valor);
            campoTexto.setDisable(true);

            campos.put(nombre, campoTexto);

            VBox grupo = new VBox(5);
            grupo.setAlignment(Pos.CENTER_LEFT);
            grupo.getChildren().addAll(new EtiquetaPersonalizada(nombre), campoTexto);
            contenedor.getChildren().add(grupo);
        }
    }

    private Button crearBotonEliminar(Map<String, String> filaSeleccionada) {
        Button eliminar = new BotonAccion("Eliminar", "#F44336");
    
        eliminar.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Estás seguro de eliminar este registro?");
            Optional<ButtonType> resultado = confirm.showAndWait();
    
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean exito = com.panaderiafx.utils.EliminarUtils.eliminarFila(nombreTabla, filaSeleccionada);
                System.out.println("🗑 Eliminando de " + nombreTabla + ": " + filaSeleccionada);
    
                if (exito) {
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Eliminado");
                    ok.setHeaderText(null);
                    ok.setContentText("✅ Registro eliminado exitosamente.");
                    ok.show();
                } else {
                    Alert fail = new Alert(Alert.AlertType.ERROR);
                    fail.setTitle("Error");
                    fail.setHeaderText(null);
                    fail.setContentText("❌ No se pudo eliminar el registro.");
                    fail.show();
                }
            }
        });
    
        return eliminar;
    }
    
}
