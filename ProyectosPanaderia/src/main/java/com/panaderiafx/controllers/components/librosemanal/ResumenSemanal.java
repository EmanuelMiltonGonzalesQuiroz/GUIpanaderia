package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import com.panaderiafx.utils.componentes.GuardarLibroSemanalUtils;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class ResumenSemanal extends VBox {

    private final Map<Tipo, TextField> campos = new HashMap<>();
    private final TextField totalField = new TextField("0.00"); 
    private Consumer<Tipo> onMostrarDetalle = null;
    private LocalDate fechaInicio;

    public void setOnMostrarDetalle(Consumer<Tipo> handler) {
        this.onMostrarDetalle = handler;
    }

    public void setFechaInicio(LocalDate fecha) {
        this.fechaInicio = fecha;
        System.out.println("📅 Fecha de inicio configurada en ResumenSemanal: " + fechaInicio);
    }

    public ResumenSemanal() {
        setSpacing(10);
        setStyle("-fx-background-color: #F36C00; -fx-padding: 15;");

        for (Tipo tipo : Tipo.values()) {
            if (tipo == Tipo.TOTAL) continue;
            TextField campo = new TextField("0.00");
            configurarCampo(campo);
            campos.put(tipo, campo);
            getChildren().add(filaConBoton(formatoVisible(tipo), campo, tipo));
        }

        configurarCampo(totalField);
        getChildren().add(filaConBoton("TOTAL", totalField, Tipo.TOTAL));

        for (Tipo tipo : Tipo.values()) {
            CacheLibroSemanal.onChange(tipo, val -> {
                if (tipo == Tipo.TOTAL) {
                    totalField.setText(String.format("%.2f", val));
                } else if (campos.containsKey(tipo)) {
                    campos.get(tipo).setText(String.format("%.2f", val));
                }
            });
        }

        Button botonGuardar = new Button("GUARDAR");
        botonGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        botonGuardar.setOnAction(e -> {
            System.out.println("🟢 Botón GUARDAR presionado desde ResumenSemanal.");

            if (fechaInicio == null) {
                System.err.println("❌ Error: fechaInicio es null en ResumenSemanal.");
                mostrarError("Debes establecer la fecha de inicio para guardar.");
                return;
            }

            LocalDate fin = fechaInicio.plusDays(6);
            System.out.println("📁 Ejecutando GuardarLibroSemanalUtils.guardar(" + fechaInicio + " → " + fin + ")");

            boolean exito = GuardarLibroSemanalUtils.guardar(fechaInicio, fin);
            if (exito) {
                mostrarInfo("Datos guardados correctamente", "Tanto el resumen semanal como las variables del día se han guardado.");
            } else {
                mostrarError("No se pudo guardar todo correctamente. Revisa los logs o intenta de nuevo.");
            }
        });

        HBox filaGuardar = new HBox(botonGuardar);
        filaGuardar.setAlignment(Pos.CENTER);
        getChildren().add(filaGuardar);
    }

    private void configurarCampo(TextField campo) {
        campo.setEditable(false);
        campo.setPrefWidth(100);
        campo.setStyle("-fx-background-color: white; -fx-font-weight: bold;");
    }

    private HBox filaConBoton(String titulo, TextField campo, Tipo tipo) {
        Button etiqueta = new Button(titulo);
        etiqueta.setPrefWidth(200);
        etiqueta.setStyle("-fx-background-color: #FFA000; -fx-text-fill: black; -fx-font-weight: bold;");

        etiqueta.setOnAction(e -> {
            if (onMostrarDetalle != null && (
                    tipo == Tipo.GANANCIA_B ||
                    tipo == Tipo.COSTO_DIRECTO ||
                    tipo == Tipo.PARAMETROS ||
                    tipo == Tipo.COSTOS_DIA ||
                    tipo == Tipo.COSTO_INDIRECTO)) {
                onMostrarDetalle.accept(tipo);
            }
        });

        HBox fila = new HBox(10, etiqueta, campo);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    /**
     * CAMBIO: Actualizado para reflejar que ahora mostramos "Beneficios" en lugar de "Ganancia B."
     */
    private String formatoVisible(Tipo tipo) {
        return switch (tipo) {
            case GANANCIA_B -> "Beneficios Totales"; // CAMBIO: Nombre más claro
            case COSTO_DIRECTO -> "Costos Directos";
            case COSTO_INDIRECTO -> "Costos Indirectos";
            case PARAMETROS -> "Parámetros";
            case COSTOS_DIA -> "Variables Extras de Día";
            case TOTAL -> "Total";
        };
    }

    private void mostrarInfo(String header, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Resumen Guardado");
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("⚠️ Error al Guardar");
        alert.setHeaderText("Ocurrió un problema");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}