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

        // Tipos fijos disponibles
        ComboBox<String> tipoCombo = new ComboBox<>(FXCollections.observableArrayList("Peso", "Volumen", "Herramienta"));
        ComboBox<String> unidadOrigenCombo = new ComboBox<>();
        ComboBox<String> unidadDestinoCombo = new ComboBox<>();
        ComboBox<String> ingredienteCombo = new ComboBox<>();

        // Extraer ingredientes únicos de la tabla
        Set<String> ingredientes = datos.stream()
            .map(d -> d.getOrDefault("Ingrediente (si aplica)", "").trim())
            .filter(i -> !i.isBlank() && !i.equals("—"))
            .collect(Collectors.toCollection(TreeSet::new));
        
        ingredienteCombo.getItems().add(""); // Opción vacía para conversiones generales
        ingredienteCombo.getItems().addAll(ingredientes);
        ingredienteCombo.setValue(""); // Seleccionar vacío por defecto

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Ej: 2.5");
        cantidadField.setFont(Font.font(16));
        cantidadField.setMaxWidth(200);

        Label resultadoLabel = new Label("👆 Selecciona los campos y presiona CALCULAR");
        resultadoLabel.setFont(Font.font(16));
        resultadoLabel.setWrapText(true);
        resultadoLabel.setMaxWidth(600);
        resultadoLabel.setStyle("-fx-padding: 10; -fx-background-color: #E3F2FD; -fx-border-radius: 5;");

        // 🔄 Actualizar unidades disponibles según el tipo seleccionado
        Runnable actualizarUnidades = () -> {
            String tipoSeleccionado = tipoCombo.getValue();
            if (tipoSeleccionado == null) return;

            Set<String> unidadesOrigen = new TreeSet<>();
            Set<String> unidadesDestino = new TreeSet<>();

            // TODAS las unidades de destino siempre disponibles (conversión cruzada)
            unidadesDestino.addAll(Set.of("Kilos", "Gramos", "Libras", "Onzas", "Litro", "Mililitros"));

            switch (tipoSeleccionado) {
                case "Peso" -> {
                    // Unidades de peso estándar
                    unidadesOrigen.addAll(Set.of("Kilos", "Gramos", "Libras", "Onzas"));
                    
                    // También agregar herramientas que convierten a peso
                    datos.stream()
                        .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
                        .forEach(d -> {
                            String unidadBase = d.getOrDefault("Unidad base", "").trim();
                            if (!unidadBase.isEmpty()) {
                                unidadesOrigen.add(unidadBase);
                                System.out.printf("🔧 Herramienta para peso: %s%n", unidadBase);
                            }
                        });
                }
                case "Volumen" -> {
                    // Unidades de volumen estándar
                    unidadesOrigen.addAll(Set.of("Litro", "Mililitros", "Onza"));
                    
                    // También herramientas que convierten a volumen
                    datos.stream()
                        .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
                        .forEach(d -> {
                            String unidadBase = d.getOrDefault("Unidad base", "").trim();
                            if (!unidadBase.isEmpty()) {
                                unidadesOrigen.add(unidadBase);
                                System.out.printf("🔧 Herramienta para volumen: %s%n", unidadBase);
                            }
                        });
                }
                case "Herramienta" -> {
                    // ✅ EXTRAER herramientas dinámicamente de la tabla
                    Set<String> herramientasUnicas = new TreeSet<>();
                    
                    datos.stream()
                        .filter(d -> "Herramienta".equals(d.getOrDefault("Tipo de medida", "")))
                        .forEach(d -> {
                            String unidadBase = d.getOrDefault("Unidad base", "").trim();
                            if (!unidadBase.isEmpty()) {
                                herramientasUnicas.add(unidadBase);
                                System.out.printf("🔧 Herramienta encontrada: %s%n", unidadBase);
                            }
                        });
                    
                    unidadesOrigen.addAll(herramientasUnicas);
                    System.out.printf("📋 Total herramientas únicas: %d%n", herramientasUnicas.size());
                    
                    // También agregar unidades estándar para conversión inversa
                    unidadesOrigen.addAll(Set.of("Kilos", "Gramos", "Libras", "Onzas", "Litro", "Mililitros", "Onza"));
                }
            }

            unidadOrigenCombo.setItems(FXCollections.observableArrayList(unidadesOrigen));
            unidadDestinoCombo.setItems(FXCollections.observableArrayList(unidadesDestino));
            
            System.out.printf("🔄 Unidades actualizadas para tipo %s: Origen=%d, Destino=%d%n", 
                             tipoSeleccionado, unidadesOrigen.size(), unidadesDestino.size());
            System.out.printf("📋 Unidades origen: %s%n", unidadesOrigen);
        };

        tipoCombo.setOnAction(e -> actualizarUnidades.run());

        Button calcularBtn = new Button("🧮 CALCULAR");
        calcularBtn.setFont(Font.font(18));
        calcularBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30; -fx-font-weight: bold;");

        calcularBtn.setOnAction(e -> {
            try {
                String cantidadText = cantidadField.getText().trim();
                if (cantidadText.isEmpty()) {
                    resultadoLabel.setText("⚠️ Ingresa una cantidad válida");
                    return;
                }

                double cantidad = Double.parseDouble(cantidadText);
                String tipo = tipoCombo.getValue();
                String unidadOrigen = unidadOrigenCombo.getValue();
                String unidadDestino = unidadDestinoCombo.getValue();
                String ingrediente = ingredienteCombo.getValue();

                if (tipo == null || unidadOrigen == null || unidadDestino == null) {
                    resultadoLabel.setText("⚠️ Por favor completa todos los campos obligatorios");
                    return;
                }

                // Si ingrediente está vacío, pasar null
                if (ingrediente != null && ingrediente.trim().isEmpty()) {
                    ingrediente = null;
                }

                System.out.printf("🎯 Intentando conversión: %.2f %s → %s (Tipo: %s, Ingrediente: %s)%n", 
                                 cantidad, unidadOrigen, unidadDestino, tipo, ingrediente);

                // Para herramientas, usar el tipo original de la tabla
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
                    resultadoLabel.setText("❌ No se encontró una conversión válida para estos parámetros.\n" +
                                         "Verifica que el ingrediente y las unidades sean compatibles.");
                    resultadoLabel.setStyle("-fx-padding: 15; -fx-background-color: #FFEBEE; -fx-border-radius: 8; -fx-border-color: #f44336;");
                } else {
                    String textoResultado = String.format("✅ %.2f %s = %.4f %s", 
                                                        cantidad, unidadOrigen, resultado, unidadDestino);
                    
                    if (ingrediente != null) {
                        textoResultado += "\n🥄 Para " + ingrediente;
                    }
                    
                    resultadoLabel.setText(textoResultado);
                    resultadoLabel.setStyle("-fx-padding: 15; -fx-background-color: #E8F5E8; -fx-border-radius: 8; -fx-border-color: #4CAF50;");
                }

            } catch (NumberFormatException ex) {
                resultadoLabel.setText("❌ El número que ingresaste no es válido");
                resultadoLabel.setStyle("-fx-padding: 15; -fx-background-color: #FFEBEE; -fx-border-radius: 8; -fx-border-color: #f44336;");
            } catch (Exception ex) {
                resultadoLabel.setText("❌ Error inesperado: " + ex.getMessage());
                resultadoLabel.setStyle("-fx-padding: 15; -fx-background-color: #FFEBEE; -fx-border-radius: 8; -fx-border-color: #f44336;");
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
            resultadoLabel.setText("👆 Selecciona los campos y presiona CALCULAR");
            resultadoLabel.setStyle("-fx-padding: 10; -fx-background-color: #E3F2FD; -fx-border-radius: 5;");
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

        formulario.getChildren().addAll(
            titulo,
            subtitulo,
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
}