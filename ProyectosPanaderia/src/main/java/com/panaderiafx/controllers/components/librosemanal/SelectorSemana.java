package com.panaderiafx.controllers.components.librosemanal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SelectorSemana extends VBox {

    private final DatePicker inicioSemana;
    private final Label rangoSemana;

    public SelectorSemana() {
        setSpacing(5);
        setStyle("-fx-background-color: #FFE0B2; -fx-padding: 10;");

        Label label = new Label("📅 Semana desde (Lunes):");
        inicioSemana = new DatePicker(ajustarALunes(LocalDate.now()));
        rangoSemana = new Label();
        actualizarRango();

        Button anterior = new Button("◀ Semana anterior");
        Button siguiente = new Button("Semana siguiente ▶");

        anterior.setOnAction(e -> {
            inicioSemana.setValue(inicioSemana.getValue().minusWeeks(1));
        });

        siguiente.setOnAction(e -> {
            inicioSemana.setValue(inicioSemana.getValue().plusWeeks(1));
        });

        // Asegurarse de que al seleccionar una fecha manual, se ajuste al lunes correspondiente
        ChangeListener<LocalDate> listener = (obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getDayOfWeek() != DayOfWeek.MONDAY) {
                inicioSemana.setValue(ajustarALunes(newVal));
            } else {
                actualizarRango();
            }
        };

        inicioSemana.valueProperty().addListener(listener);

        HBox fila = new HBox(10, label, inicioSemana, anterior, siguiente);
        fila.setStyle("-fx-padding: 5;");

        getChildren().addAll(fila, rangoSemana);
    }

    private void actualizarRango() {
        LocalDate ini = getFechaInicioSemana();
        LocalDate fin = getFechaFinSemana();
        rangoSemana.setText("Semana: " + ini.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " al " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private LocalDate ajustarALunes(LocalDate fecha) {
        return fecha.with(DayOfWeek.MONDAY);
    }

    public LocalDate getFechaInicioSemana() {
        return inicioSemana.getValue();
    }

    public LocalDate getFechaFinSemana() {
        return inicioSemana.getValue().plusDays(6);
    }

    public ObjectProperty<LocalDate> valueProperty() {
        return inicioSemana.valueProperty();
    }
}
