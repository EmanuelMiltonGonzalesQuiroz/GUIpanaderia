package com.panaderiafx.controllers;

import com.panaderiafx.utils.ConversorUtils2;
import com.panaderiafx.utils.VerUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.*;
import java.util.stream.Collectors;

public class CalculadoraConversion {

    public static Pane crearVista() {
        List<Map<String, String>> datos = VerUtils.verTabla("TabladeConversión");

        ComboBox<String> tipoCombo = new ComboBox<>(FXCollections.observableArrayList("Peso", "Volumen", "Herramienta"));
        ComboBox<String> unidadOrigenCombo = new ComboBox<>();
        ComboBox<String> unidadDestinoCombo = new ComboBox<>();
        ComboBox<String> ingredienteCombo = new ComboBox<>();

        // Cargar ingredientes únicos
        Set<String> ingredientes = datos.stream()
                .map(d -> d.getOrDefault("Ingrediente (si aplica)", "").trim())
                .filter(i -> !i.isBlank() && !i.equals("—"))
                .collect(Collectors.toCollection(TreeSet::new));

        ingredienteCombo.getItems().add("");
        ingredienteCombo.getItems().addAll(ingredientes);
        ingredienteCombo.setValue("");

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Ej: 2.5");
        cantidadField.setFont(Font.font(16));
        cantidadField.setMaxWidth(200);

        Label resultadoLabel = new Label("👆 Selecciona los campos y presiona CALCULAR");
        resultadoLabel.setFont(Font.font(16));
        resultadoLabel.setWrapText(true);
        resultadoLabel.setMaxWidth(600);
        resultadoLabel.setStyle("-fx-padding: 10; -fx-background-color: #E3F2FD; -fx-border-radius: 5;");

        // Función para obtener todas las unidades disponibles (para unidad destino)
        Runnable cargarTodasLasUnidades = () -> {
            Set<String> todasLasUnidades = new TreeSet<>();
            
            datos.forEach(d -> {
                String unidadBase = d.getOrDefault("Unidad base", "").trim();
                String unidad2 = d.getOrDefault("Unidad 2", "").trim();
                
                if (!unidadBase.isEmpty() && !unidadBase.equals("—")) {
                    todasLasUnidades.add(unidadBase);
                }
                if (!unidad2.isEmpty() && !unidad2.equals("—")) {
                    todasLasUnidades.add(unidad2);
                }
            });
            
            unidadDestinoCombo.setItems(FXCollections.observableArrayList(todasLasUnidades));
        };

        // Función para actualizar unidades de origen según el tipo seleccionado
        Runnable actualizarUnidadesOrigen = () -> {
            String tipoSeleccionado = tipoCombo.getValue();
            if (tipoSeleccionado == null) {
                unidadOrigenCombo.getItems().clear();
                return;
            }

            Set<String> unidadesOrigen = new TreeSet<>();

            
            datos.forEach(d -> {
                String codigo = d.getOrDefault("Código", "").trim();
                String tipoMedida = d.getOrDefault("Tipo de medida", "").trim();
                String tipoLogico = d.getOrDefault("Tipo lógico", "").trim();
                String unidadBase = d.getOrDefault("Unidad base", "").trim();
                
                boolean incluirUnidad = false;
                String razon = "";
                
                // SOLO para PESO: buscar registros con Tipo lógico = "Peso"
                if ("Peso".equalsIgnoreCase(tipoSeleccionado)) {
                    if ("Peso".equalsIgnoreCase(tipoLogico)) {
                        incluirUnidad = true;
                        razon = "Tipo lógico = Peso";
                    }
                }
                // SOLO para VOLUMEN: buscar registros con Tipo lógico = "Volumen"  
                else if ("Volumen".equalsIgnoreCase(tipoSeleccionado)) {
                    if ("Volumen".equalsIgnoreCase(tipoLogico)) {
                        incluirUnidad = true;
                        razon = "Tipo lógico = Volumen";
                    }
                }
                // SOLO para HERRAMIENTA: buscar registros con Tipo de medida = "Herramienta"
                else if ("Herramienta".equalsIgnoreCase(tipoSeleccionado)) {
                    if ("Herramienta".equalsIgnoreCase(tipoMedida)) {
                        incluirUnidad = true;
                        razon = "Tipo medida = Herramienta";
                    }
                }
                
                    unidadesOrigen.add(unidadBase);
                 
            });

            unidadOrigenCombo.setItems(FXCollections.observableArrayList(unidadesOrigen));
        };

        // Configurar eventos
        tipoCombo.setOnAction(e -> {
            actualizarUnidadesOrigen.run();
            cargarTodasLasUnidades.run();
        });

        // Cargar todas las unidades al inicio
        cargarTodasLasUnidades.run();

        Button calcularBtn = new Button("🧮 CALCULAR");
        calcularBtn.setFont(Font.font(18));
        calcularBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30; -fx-font-weight: bold;");

        calcularBtn.setOnAction(e -> {
            try {
                String cantidadText = cantidadField.getText().trim();
                if (cantidadText.isEmpty()) {
                    mostrarResultado(resultadoLabel, "⚠️ Ingresa una cantidad válida", "warning");
                    return;
                }

                double cantidad = Double.parseDouble(cantidadText);
                String tipo = tipoCombo.getValue();
                String unidadOrigen = unidadOrigenCombo.getValue();
                String unidadDestino = unidadDestinoCombo.getValue();
                String ingrediente = ingredienteCombo.getValue();

                if (tipo == null || unidadOrigen == null || unidadDestino == null) {
                    mostrarResultado(resultadoLabel, "⚠️ Por favor completa todos los campos obligatorios", "warning");
                    return;
                }

                if (ingrediente != null && ingrediente.trim().isEmpty()) {
                    ingrediente = null;
                }

                String tipoParaConversion = tipo;
                if ("Herramienta".equals(tipo)) {
                    tipoParaConversion = "Herramienta";
                }

                Double resultado = ConversorUtils2.convertir(
                        tipoParaConversion,
                        unidadOrigen,
                        unidadDestino,
                        cantidad,
                        ingrediente
                );

                if (resultado == null) {
                    String mensaje = "❌ No se encontró una conversión válida para estos parámetros.\n" +
                            "Verifica que el ingrediente y las unidades sean compatibles.";
                    mostrarResultado(resultadoLabel, mensaje, "error");
                } else {
                    String textoResultado = String.format("✅ %.2f %s = %.4f %s",
                            cantidad, unidadOrigen, resultado, unidadDestino);

                    if (ingrediente != null) {
                        textoResultado += "\n🥄 Para " + ingrediente;
                    }

                    mostrarResultado(resultadoLabel, textoResultado, "success");
                }

            } catch (NumberFormatException ex) {
                mostrarResultado(resultadoLabel, "❌ El número que ingresaste no es válido", "error");
            } catch (Exception ex) {
                mostrarResultado(resultadoLabel, "❌ Error inesperado: " + ex.getMessage(), "error");
                ex.printStackTrace();
            }
        });

        Button limpiarBtn = new Button("🧹 LIMPIAR");
        limpiarBtn.setFont(Font.font(14));
        limpiarBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 12 25; -fx-font-weight: bold;");
        limpiarBtn.setOnAction(e -> {
            cantidadField.clear();
            tipoCombo.setValue(null);
            unidadOrigenCombo.getItems().clear();
            unidadDestinoCombo.getItems().clear();
            ingredienteCombo.setValue("");
            mostrarResultado(resultadoLabel, "👆 Selecciona los campos y presiona CALCULAR", "default");
            cargarTodasLasUnidades.run(); // Recargar las unidades destino
        });

        VBox formulario = new VBox(20);
        formulario.setAlignment(Pos.CENTER_LEFT);
        formulario.setPadding(new Insets(30));
        formulario.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #ddd; -fx-border-radius: 10;");
        formulario.setMaxWidth(800);

        Label titulo = new Label("🔄 CALCULADORA DE CONVERSIONES");
        titulo.setFont(Font.font(26));
        titulo.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        Label subtitulo = new Label("Convierte entre Peso, Volumen y Herramientas de cocina");
        subtitulo.setFont(Font.font(16));
        subtitulo.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");

        // Crear descripción de la lógica
        Label descripcion = new Label("💡 Unidad origen: según tipo lógico seleccionado | Unidad destino: todas disponibles");
        descripcion.setFont(Font.font(12));
        descripcion.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
        descripcion.setWrapText(true);

        formulario.getChildren().addAll(
                titulo,
                subtitulo,
                descripcion,
                new Separator(),
                crearFila("📏 Tipo de medida:", tipoCombo),
                crearFila("🥄 Ingrediente (opcional):", ingredienteCombo),
                crearFila("🔢 Cantidad:", cantidadField),
                crearFila("📥 Unidad origen:", unidadOrigenCombo),
                crearFila("📤 Unidad destino:", unidadDestinoCombo),
                new Separator(),
                new HBox(15, calcularBtn, limpiarBtn),
                new Separator(),
                resultadoLabel
        );

        VBox wrapper = new VBox(formulario);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(30));
        wrapper.setStyle("-fx-background-color: linear-gradient(to bottom, #E8F5E8, #F1F8E9);");

        return wrapper;
    }

    private static HBox crearFila(String etiqueta, Control campo) {
        Label label = new Label(etiqueta);
        label.setFont(Font.font(16));
        label.setMinWidth(200);
        label.setStyle("-fx-font-weight: bold;");

        campo.setStyle("-fx-font-size: 14; -fx-padding: 8;");
        if (campo instanceof ComboBox) {
            campo.setMinWidth(250);
        }

        HBox fila = new HBox(20, label, campo);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private static void mostrarResultado(Label label, String mensaje, String tipo) {
        label.setText(mensaje);
        
        switch (tipo) {
            case "success":
                label.setStyle("-fx-padding: 15; -fx-background-color: #E8F5E8; -fx-border-radius: 8; -fx-border-color: #4CAF50;");
                break;
            case "error":
                label.setStyle("-fx-padding: 15; -fx-background-color: #FFEBEE; -fx-border-radius: 8; -fx-border-color: #f44336;");
                break;
            case "warning":
                label.setStyle("-fx-padding: 15; -fx-background-color: #FFF3E0; -fx-border-radius: 8; -fx-border-color: #FF9800;");
                break;
            default:
                label.setStyle("-fx-padding: 10; -fx-background-color: #E3F2FD; -fx-border-radius: 5;");
                break;
        }
    }
}