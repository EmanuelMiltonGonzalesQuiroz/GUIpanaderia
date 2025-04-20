package com.panaderiafx.controllers;

import com.panaderiafx.utils.ConversorUtils;
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

    private static final Map<String, List<String>> TIPOS_COMPATIBLES = Map.of(
        "Herramienta", List.of("Herramienta", "Peso", "Volumen"),
        "Peso", List.of("Peso", "Herramienta"),
        "Volumen", List.of("Volumen", "Herramienta")
    );

    private static final Map<String, Set<String>> UNIDADES_POR_TIPO = Map.of(
        "Peso", Set.of("Gramos", "Kilos", "Libras", "Onzas"),
        "Volumen", Set.of("Mililitros", "Litro", "Onza"),
        "Herramienta", Set.of("1 taza", "1/2 taza", "1/3 taza", "1/4 taza",
                              "1 cucharada (tbsp)", "1 cucharadita (tsp)")
    );

    public static Pane crearVista() {
        List<Map<String, String>> datos = VerUtils.verTabla("TabladeConversión");

        ComboBox<String> tipoCombo = new ComboBox<>(FXCollections.observableArrayList("Peso", "Volumen", "Herramienta"));
        ComboBox<String> unidadOrigenCombo = new ComboBox<>();
        ComboBox<String> unidadDestinoCombo = new ComboBox<>();
        ComboBox<String> ingredienteCombo = new ComboBox<>();

        Set<String> ingredientes = datos.stream()
            .map(d -> d.getOrDefault("Ingrediente (si aplica)", "").trim())
            .filter(i -> !i.isBlank() && !i.equalsIgnoreCase("—"))
            .collect(Collectors.toCollection(TreeSet::new));
        ingredienteCombo.getItems().add("");
        ingredienteCombo.getItems().addAll(ingredientes);

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Ej: 5");
        cantidadField.setFont(Font.font(16));
        cantidadField.setMaxWidth(200);

        Label resultadoLabel = new Label("");
        resultadoLabel.setFont(Font.font(18));
        resultadoLabel.setWrapText(true);
        resultadoLabel.setMaxWidth(600);

        // 🔁 Recarga de unidades por tipo
        Runnable recargarUnidades = () -> {
            String tipo = tipoCombo.getValue();
            if (tipo == null) return;

            Set<String> unidadesValidasOrigen = UNIDADES_POR_TIPO.getOrDefault(tipo, Set.of());
            unidadOrigenCombo.setItems(FXCollections.observableArrayList(unidadesValidasOrigen));

            List<String> tiposDestino = TIPOS_COMPATIBLES.getOrDefault(tipo, List.of(tipo));
            Set<String> unidadesDestinoPermitidas = tiposDestino.stream()
                .flatMap(t -> UNIDADES_POR_TIPO.getOrDefault(t, Set.of()).stream())
                .collect(Collectors.toCollection(TreeSet::new));
            unidadDestinoCombo.setItems(FXCollections.observableArrayList(unidadesDestinoPermitidas));
        };

        tipoCombo.setOnAction(e -> recargarUnidades.run());
        ingredienteCombo.setOnAction(e -> recargarUnidades.run());

        Button calcularBtn = new Button("CALCULAR");
        calcularBtn.setFont(Font.font(18));
        calcularBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20 10 20;");

        calcularBtn.setOnAction(e -> {
            try {
                double cantidad = Double.parseDouble(cantidadField.getText().trim());
                String tipo = tipoCombo.getValue();
                String unidadOrigen = unidadOrigenCombo.getValue();
                String unidadDestino = unidadDestinoCombo.getValue();
                String ingrediente = ingredienteCombo.getValue();

                if (tipo == null || unidadOrigen == null || unidadDestino == null) {
                    resultadoLabel.setText("⚠️ Por favor completa todos los campos.");
                    return;
                }

                Double resultado = ConversorUtils.convertir(
                    tipo,
                    unidadOrigen,
                    unidadDestino,
                    cantidad,
                    ingrediente
                );

                if (resultado == null) {
                    resultadoLabel.setText("❌ No se encontró conversión válida con ese ingrediente.");
                } else {
                    resultadoLabel.setText("✅ Resultado: " + String.format("%.2f", resultado) + " " + unidadDestino);
                }

            } catch (Exception ex) {
                resultadoLabel.setText("❌ Revisa el número que escribiste.");
            }
        });

        VBox formulario = new VBox(20);
        formulario.setAlignment(Pos.CENTER_LEFT);
        formulario.setPadding(new Insets(30));
        formulario.setStyle("-fx-background-color: #FFF3E0; -fx-border-color: #ccc;");
        formulario.setMaxWidth(800);

        formulario.getChildren().addAll(
            crearFila("¿Qué tipo de medida es?", tipoCombo),
            crearFila("¿Qué ingrediente usas? (opcional)", ingredienteCombo),
            crearFila("¿Cuánto vas a usar?", cantidadField),
            crearFila("¿En qué unidad lo tenés?", unidadOrigenCombo),
            crearFila("¿A qué unidad lo querés convertir?", unidadDestinoCombo),
            calcularBtn,
            resultadoLabel
        );

        VBox wrapper = new VBox(formulario);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(50));
        wrapper.setStyle("-fx-background-color: #FFF3E0;");
        return wrapper;
    }

    private static HBox crearFila(String etiqueta, Control campo) {
        Label label = new Label(etiqueta);
        label.setFont(Font.font(18));
        label.setMinWidth(250);
        campo.setStyle("-fx-font-size: 16;");
        HBox fila = new HBox(20, label, campo);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }
}
