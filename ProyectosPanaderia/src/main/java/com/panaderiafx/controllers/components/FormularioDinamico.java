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
    private TextField campoPrecioLocal;
    private TextField campoPrecioDolar;
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
        for (Map<String, Object> campo : camposDefinidos) {
            String nombre = (String) campo.get("nombre");
            String tipo = (String) campo.get("tipo");
            Node input;

            switch (tipo.toLowerCase()) {
                case "codigo" -> {
                    codigoGenerado = CodigoGenerator.generarCodigo(nombreTabla, "Código"); // automático
                    input = new Label(codigoGenerado);
                }
                case "select" -> input = new ListaSeleccion(nombreTabla, nombre);
                case "precio local" -> {
                    campoPrecioLocal = new CampoTexto("Ingrese valor local");
                    input = campoPrecioLocal;
                }
                case "precio dólar" -> {
                    campoPrecioDolar = new CampoTexto("Ingrese valor dólar");
                    input = campoPrecioDolar;
                }
                case "fecha" -> {
                    String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    input = new Label(fechaHoy);
                }
                default -> input = new CampoTexto("Ingrese un valor...");
            }

            campos.put(nombre, input);

            VBox grupo = new VBox(5);
            grupo.setAlignment(Pos.CENTER_LEFT);
            grupo.getChildren().addAll(new EtiquetaPersonalizada(nombre), input);
            contenedor.getChildren().add(grupo);
        }

        if (campoPrecioLocal != null && campoPrecioDolar != null) {
            campoPrecioLocal.textProperty().addListener((obs, oldVal, newVal) -> {
                if (bloqueado || newVal.isEmpty() || !campoPrecioLocal.isFocused()) return;
                try {
                    bloqueado = true;
                    double local = Double.parseDouble(newVal);
                    double tasa = TasaCambioUtils.obtenerUltimaTasa();
                    double dolar = local / tasa;
                    campoPrecioDolar.setText(String.format("%.4f", dolar));
                    System.out.println("💰 Local ➤ Dólar: " + local + " ➤ " + dolar + " (tasa " + tasa + ")");
                } catch (Exception e) {
                    campoPrecioDolar.setText("");
                    System.out.println("⚠️ Error Local ➤ Dólar: " + e.getMessage());
                } finally {
                    bloqueado = false;
                }
            });

            campoPrecioDolar.textProperty().addListener((obs, oldVal, newVal) -> {
                if (bloqueado || newVal.isEmpty() || !campoPrecioDolar.isFocused()) return;
                try {
                    bloqueado = true;
                    double dolar = Double.parseDouble(newVal);
                    double tasa = TasaCambioUtils.obtenerUltimaTasa();
                    double local = dolar * tasa;
                    campoPrecioLocal.setText(String.format("%.4f", local));
                    System.out.println("💲 Dólar ➤ Local: " + dolar + " ➤ " + local + " (tasa " + tasa + ")");
                } catch (Exception e) {
                    campoPrecioLocal.setText("");
                    System.out.println("⚠️ Error Dólar ➤ Local: " + e.getMessage());
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

            System.out.println("📤 Intentando guardar en tabla: " + nombreTabla);
            System.out.println("📦 Datos: " + datos);

            boolean exito = CrearUtils.crearFila(nombreTabla, datos);
            if (exito) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Éxito");
                ok.setHeaderText(null);
                ok.setContentText("✅ Fila guardada exitosamente.");
                ok.show();
            } else {
                Alert fail = new Alert(Alert.AlertType.ERROR);
                fail.setTitle("Error");
                fail.setHeaderText(null);
                fail.setContentText("❌ No se pudo guardar la fila.");
                fail.show();
            }
        });

        return guardar;
    }
}
