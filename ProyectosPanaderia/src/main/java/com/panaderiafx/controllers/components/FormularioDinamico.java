package com.panaderiafx.controllers.components;

import com.panaderiafx.utils.CrearUtils;
import com.panaderiafx.utils.TasaCambioUtils;
import com.panaderiafx.utils.CodigoGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FormularioDinamico extends ContenedorFlexible {

    private final Map<String, Node> campos = new LinkedHashMap<>();
    private final String nombreTabla;
    private CampoTexto campoPrecioLocal;
    private CampoTexto campoPrecioDolar;
    private boolean bloqueado = false;
    private String codigoGenerado = "";

    public FormularioDinamico(String nombreTabla, List<Map<String, Object>> definicionCampos) {
        this.nombreTabla = nombreTabla;
        VBox formulario = new VBox(15);
        formulario.setPadding(new Insets(20));
        formulario.setAlignment(Pos.CENTER_LEFT);
        formulario.setMaxWidth(500);
        formulario.setStyle("-fx-background-color: #FFF3E0;");

        construirFormulario(definicionCampos, formulario);
        formulario.getChildren().add(crearBotonGuardar());
        this.setContenido(formulario);
    }

    private void construirFormulario(List<Map<String, Object>> camposDefinidos, VBox contenedor) {
        List<CampoTexto> camposPrecio = new ArrayList<>();

        for (Map<String, Object> campo : camposDefinidos) {
            String nombre = (String) campo.get("nombre");
            String tipo = ((String) campo.get("tipo")).toLowerCase();
            Node input;

            switch (tipo) {
                case "código" -> {
                    codigoGenerado = CodigoGenerator.generarCodigo(nombreTabla, "Código"); // automático
                    input = new Label(codigoGenerado);
                }
                case "select" -> {
                    String origen = (String) campo.getOrDefault("origen", nombreTabla);
                    String mostrar = (String) campo.getOrDefault("datoMostrar", nombre);
                    String cargar = (String) campo.getOrDefault("datoCargar", nombre);
                    input = new CampoSeleccionExtendido(origen, mostrar, cargar, "");
                }
                case "precio" -> {
                    CampoTexto campoPrecio = new CampoTexto("Ingrese precio...");
                    campoPrecio.setText(null);
                    camposPrecio.add(campoPrecio);
                    input = campoPrecio;
                }
                case "fecha" -> {
                    String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    input = new Label(fechaHoy);
                }
                default -> {
                    CampoTexto campoTexto = new CampoTexto("Ingrese un valor...");
                    input = campoTexto;
                }
            }

            campos.put(nombre, input);

            VBox grupo = new VBox(5);
            grupo.setAlignment(Pos.CENTER_LEFT);
            grupo.getChildren().addAll(new EtiquetaPersonalizada(nombre), input);
            contenedor.getChildren().add(grupo);
        }

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
    private Button crearBotonGuardar() {
        Button guardar = new BotonAccion("Guardar", "#4CAF50");

        guardar.setOnAction(e -> {
            Map<String, String> datos = new LinkedHashMap<>();
            StringBuilder errores = new StringBuilder();

            for (var entrada : campos.entrySet()) {
                String nombre = entrada.getKey();
                Node nodo = entrada.getValue();
                String valor = "";

                if (nodo instanceof CampoTexto campo) {
                    valor = campo.getText().trim();
                } else if (nodo instanceof ListaSeleccion lista) {
                    valor = lista.getValorSeleccionado();
                } else if (nodo instanceof Label label) {
                    valor = label.getText().trim();
                } else if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                    valor = campoExtendido.getValorSeleccionado();
                }

                if ((valor == null || valor.isEmpty()) && !nombre.toLowerCase().contains("código") && !nombre.toLowerCase().contains("fecha")) {
                    errores.append("El campo ").append(nombre).append(" es obligatorio.\n");
                }

                datos.put(nombre, valor);
            }

            if (!datos.containsKey("Código") && !codigoGenerado.isEmpty()) {
                datos.put("Código", codigoGenerado);
            }

            if (!datos.containsKey("Fecha de actualización")) {
                datos.put("Fecha de actualización", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            if (errores.length() > 0) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Errores");
                alerta.setHeaderText("Corrige lo siguiente:");
                alerta.setContentText(errores.toString());
                alerta.show();
                return;
            }

            boolean exito = CrearUtils.crearFila(nombreTabla, datos);
            Alert resultado = new Alert(exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            resultado.setTitle(exito ? "Éxito" : "Error");
            resultado.setHeaderText(null);
            resultado.setContentText(exito ? "✅ Fila guardada exitosamente." : "❌ No se pudo guardar la fila.");
            resultado.show();
        });

        return guardar;
    }

    public Map<String, Node> getCampos() {
        return campos;
    }
}
