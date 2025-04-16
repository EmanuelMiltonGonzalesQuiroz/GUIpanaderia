package com.panaderiafx.controllers.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.panaderiafx.utils.TasaCambioUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FormularioModificar extends ContenedorFlexible { 

    private final String nombreTabla;
    private final Map<String, Node> campos = new LinkedHashMap<>();
    private TextField campoPrecioLocal;
    private TextField campoPrecioDolar;
    private boolean bloqueado = false;
    private final List<Map<String, String>> registros;
    private final List<Map<String, Object>> definicionCampos;

    public FormularioModificar(String nombreTabla, List<Map<String, Object>> definicionCampos, List<Map<String, String>> registros, Map<String, String> filaSeleccionada) {
        this.nombreTabla = nombreTabla;
        this.registros = registros;
        this.definicionCampos = definicionCampos;
    
        VBox contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setAlignment(Pos.CENTER_LEFT);
        contenedor.setMaxWidth(500);
        contenedor.setStyle("-fx-background-color: #FFF3E0;");
    
        VBox formulario = new VBox(15);
        construirFormulario(filaSeleccionada, formulario);
        
        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_LEFT);
        botones.getChildren().addAll(
            crearBotonModificar(filaSeleccionada),
            crearBotonEliminar(filaSeleccionada)
        );
        formulario.getChildren().add(botones);
    
        contenedor.getChildren().add(formulario);
        this.setContenido(contenedor);
    }
    
    private void construirFormulario(Map<String, String> valores, VBox contenedor) {
    List<CampoTexto> camposPrecio = new ArrayList<>();

    for (Map<String, Object> campoDef : definicionCampos) {
        String nombre = (String) campoDef.get("nombre");
        String tipo = (String) campoDef.get("tipo");
        String valor = valores.getOrDefault(nombre, "");
        Node input;


        switch (tipo.toLowerCase()) {
            case "label" -> input = new Label(valor);
            case "select" -> input = new CampoSeleccionExtendido(nombreTabla, nombre, valor); 
            case "precio" -> {
                CampoTexto campoPrecio = new CampoTexto("Ingrese precio...");
                campoPrecio.setText(valor);
                camposPrecio.add(campoPrecio);
                input = campoPrecio;
            }
            case "fecha" -> input = new Label(valor);
            default -> {
                CampoTexto campoTexto = new CampoTexto("Modificar valor...");
                campoTexto.setText(valor);
                input = campoTexto;
            }
        }

        campos.put(nombre, input);

        VBox grupo = new VBox(5);
        grupo.setAlignment(Pos.CENTER_LEFT);
        grupo.getChildren().addAll(new EtiquetaPersonalizada(nombre), input);
        contenedor.getChildren().add(grupo);
    }

    // Si hay exactamente 2 campos tipo "precio", enlazar lógica de sincronización
    if (camposPrecio.size() == 2) {
        CampoTexto campo1 = camposPrecio.get(0);
        CampoTexto campo2 = camposPrecio.get(1);

        campo1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueado || newVal.isBlank() || !campo1.isFocused()) return;
            try {
                bloqueado = true;
                double local = Double.parseDouble(newVal);
                double tasa = TasaCambioUtils.obtenerUltimaTasa();
                double convertido = local * tasa;
                campo2.setText(String.format("%.4f", convertido));
            } catch (Exception e) {
                campo2.setText("");
            } finally {
                bloqueado = false;
            }
        });

        campo2.textProperty().addListener((obs, oldVal, newVal) -> {
            if (bloqueado || newVal.isBlank() || !campo2.isFocused()) return;
            try {
                bloqueado = true;
                double dolar = Double.parseDouble(newVal);
                double tasa = TasaCambioUtils.obtenerUltimaTasa();
                double convertido = dolar / tasa;
                campo1.setText(String.format("%.4f", convertido));
            } catch (Exception e) {
                campo1.setText("");
            } finally {
                bloqueado = false;
            }
        });
    }
}

    private Button crearBotonModificar(Map<String, String> original) {
        Button modificar = new BotonAccion("Actualizar", "#FF9800");
    
        modificar.setOnAction(e -> {
            Map<String, String> nuevos = new LinkedHashMap<>();
    
            for (var entrada : campos.entrySet()) {
                String campo = entrada.getKey();
                Node nodo = entrada.getValue();
                String valor = "";
    
                if (nodo instanceof CampoTexto texto) {
                    valor = texto.getText().trim();
                } else if (nodo instanceof ListaSeleccion lista) {
                    valor = lista.getValorSeleccionado();
                } else if (nodo instanceof CampoSeleccionExtendido extendido) {
                    valor = extendido.getValorSeleccionado();
                } else if (nodo instanceof Label label) {
                    valor = label.getText().trim();
                }
    
                if (valor == null) valor = ""; // prevenir errores por null
                nuevos.put(campo, valor);
            }
    
            System.out.println("🛠 Modificando: " + original);
            System.out.println("➡️ Nuevo valor: " + nuevos);
    
            boolean exito = com.panaderiafx.utils.ModificarUtils.modificarFila(nombreTabla, original, nuevos);
    
            Alert alerta = new Alert(exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alerta.setTitle(exito ? "Éxito" : "Error");
            alerta.setHeaderText(null);
            alerta.setContentText(exito ? "✅ Registro modificado exitosamente." : "❌ No se pudo modificar el registro.");
            alerta.show();
        });
    
        return modificar;
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
    
                Alert alerta = new Alert(exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                alerta.setTitle(exito ? "Eliminado" : "Error");
                alerta.setHeaderText(null);
                alerta.setContentText(exito ? "✅ Registro eliminado exitosamente." : "❌ No se pudo eliminar el registro.");
                alerta.show();
            }
        });
    
        return eliminar;
    }

    public Map<String, Node> getCampos() {
        return campos;
    }
    
    
}
