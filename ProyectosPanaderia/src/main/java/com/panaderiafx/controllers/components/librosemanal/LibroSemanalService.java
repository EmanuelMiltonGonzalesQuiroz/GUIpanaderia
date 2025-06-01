package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.CostosIndirectosUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibroSemanalService {

    public static void cargarResumenSemanal(LocalDate inicio) {
        LocalDate fin = inicio.plusDays(6);
        List<Map<String, String>> ganancias = VerUtils.verTabla("Produccion");
        List<Map<String, String>> variableDia = VerUtils.verTabla("VariableDia");

        double gananciaTotal = 0;
        double costosDirectos = 0;
        double variableDiaTotal = 0;

        // ✅ Ganancias y Costos Directos
        for (Map<String, String> fila : ganancias) {
            LocalDate fecha = ParseUtils.toDate(fila.get("Fecha"));
            if (fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin)) {
                gananciaTotal += ParseUtils.toDouble(fila.get("Ganancia Total")); // corregido nombre
                costosDirectos += ParseUtils.toDouble(fila.get("Costo Total"));
            }
        }

        // ✅ VariableDia filtrada por fecha y efecto
        for (Map<String, String> fila : variableDia) {
            LocalDate fecha = ParseUtils.toDate(fila.get("Fecha"));
            if (fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin)) {
                double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
                String efecto = fila.getOrDefault("EFECTO", "-");
                if (efecto.equals("+")) {
                    variableDiaTotal += valor;
                } else {
                    variableDiaTotal -= valor;
                }
            }
        }

        double costosIndirectos = CostosIndirectosUtils.calcular("SEMANA");
        double parametros = calcularParametrosIntegrado(gananciaTotal);

        System.out.println("📊 RESUMEN SEMANAL:");
        System.out.println("   Ganancia Total: " + gananciaTotal);
        System.out.println("   Costos Directos: " + costosDirectos);
        System.out.println("   Costos Indirectos: " + costosIndirectos);
        System.out.println("   Parámetros: " + parametros);
        System.out.println("   Costos Extra (VariableDia): " + variableDiaTotal);

        CacheLibroSemanal.set(Tipo.GANANCIA_B, gananciaTotal);
        CacheLibroSemanal.set(Tipo.COSTO_DIRECTO, costosDirectos);
        CacheLibroSemanal.set(Tipo.COSTO_INDIRECTO, costosIndirectos);
        CacheLibroSemanal.set(Tipo.PARAMETROS, parametros);
        CacheLibroSemanal.set(Tipo.COSTOS_DIA, variableDiaTotal);
    }

    private static double calcularParametrosIntegrado(double ganancia) {
        List<Map<String, String>> parametros = VerUtils.verTabla("Parametros");

        for (Map<String, String> fila : parametros) {
            fila.putIfAbsent("Check", "✓");
        }

        double total = 0.0;
        double manoObra = 0.0;
        int empleados = 1;
        boolean activoManoObra = false;
        boolean activoEmpleados = false;

        for (Map<String, String> fila : parametros) {
            if (!"✓".equals(fila.getOrDefault("Check", ""))) continue;

            String codigo = fila.getOrDefault("Código", "").trim();
            double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
            String unidad = fila.getOrDefault("Unidad", "").trim().toLowerCase();

            switch (codigo) {
                case "PAR0001" -> {
                    manoObra = valor;
                    activoManoObra = true;
                }
                case "PAR0002" -> {
                    empleados = (int) valor;
                    activoEmpleados = true;
                }
                default -> {
                    if (unidad.contains("%")) {
                        double desc = ganancia * (valor / 100.0);
                        total += desc;
                    }
                }
            }
        }

        if (activoManoObra && activoEmpleados) {
            total += manoObra * empleados;
        }

        return total;
    }

    public static void cargarDetallePorDia(LocalDate inicio, DetallePorDiaVista detalle) {
        List<Map<String, String>> ganancias = VerUtils.verTabla("Produccion");

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = inicio.plusDays(i);
            List<Map<String, String>> filasDia = ganancias.stream()
                    .filter(f -> ParseUtils.toDate(f.get("Fecha")) != null)
                    .filter(f -> ParseUtils.toDate(f.get("Fecha")).equals(fecha))
                    .collect(Collectors.toList());

            TableView<Map<String, String>> tabla = detalle.getTablaPorFecha(fecha);
            if (tabla != null) {
                tabla.getItems().setAll(filasDia);
                if (tabla.getColumns().isEmpty()) {
                    agregarColumnas(tabla);
                }
                if (!filasDia.isEmpty()) {
                    detalle.mostrarSiTieneDatos(fecha);
                }
            }
        }
    }

    private static void agregarColumnas(TableView<Map<String, String>> tabla) {
        String[] columnas = {
                "Producto", "Cantidad Producida", "Precio de Venta por Unidad",
                "Costo Directo/U", "Costo Total", "Ganancia Total" // corregido
        };
        for (String col : columnas) {
            TableColumn<Map<String, String>, String> c = new TableColumn<>(col);
            c.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(col, "")));
            tabla.getColumns().add(c);
        }
    }
}
