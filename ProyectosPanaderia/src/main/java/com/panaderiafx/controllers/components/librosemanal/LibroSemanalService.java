package com.panaderiafx.controllers.components.librosemanal;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.cache.CacheLibroSemanal;
import com.panaderiafx.utils.cache.CacheLibroSemanal.Tipo;
import com.panaderiafx.utils.componentes.ParseUtils;
import com.panaderiafx.utils.componentes.CostosIndirectosUtils;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.*;

public class LibroSemanalService {

    public static void cargarResumenSemanal(LocalDate fechaInicio) {
        System.out.println("📊 Cargando resumen semanal desde: " + fechaInicio);
        
        LocalDate fechaFin = fechaInicio.plusDays(6);
        
        // CAMBIO: Ahora usamos la tabla "Ventas" en lugar de "Produccion" para beneficios y costos directos
        List<Map<String, String>> ventas = VerUtils.verTabla("Ventas");
        List<Map<String, String>> variableDia = VerUtils.verTabla("VariableDia");
        
        double totalBeneficios = 0.0;
        double totalCostosDirectos = 0.0;
        double variableDiaTotal = 0.0;
        
        // ✅ NUEVO: Calcular beneficios y costos directos desde tabla "Ventas"
        for (Map<String, String> venta : ventas) {
            LocalDate fecha = ParseUtils.toDate(venta.get("Fecha"));
            if (fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin)) {
                // Sumar beneficios de ventas
                double beneficio = ParseUtils.toDouble(venta.getOrDefault("Beneficio", "0"));
                totalBeneficios += beneficio;
                
                // Calcular costos directos de ventas
                double cantidadVendida = ParseUtils.toDouble(venta.getOrDefault("Cantidad Vendida", "0"));
                double costoDirectoUnidad = ParseUtils.toDouble(venta.getOrDefault("Costo Directo/U", "0"));
                double costoDirectoTotal = cantidadVendida * costoDirectoUnidad;
                totalCostosDirectos += costoDirectoTotal;
                
                System.out.println("   📈 Venta del " + fecha + 
                                 " - Beneficio: " + beneficio + 
                                 " - Costo Directo: " + costoDirectoTotal);
            }
        }
        
        // ✅ ORIGINAL: VariableDia filtrada por fecha y efecto (mantener lógica original)
        for (Map<String, String> fila : variableDia) {
            LocalDate fecha = ParseUtils.toDate(fila.get("Fecha"));
            if (fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin)) {
                double valor = ParseUtils.toDouble(fila.getOrDefault("Valor", "0"));
                String efecto = fila.getOrDefault("EFECTO", "-");
                if (efecto.equals("+")) {
                    variableDiaTotal += valor;
                } else {
                    variableDiaTotal -= valor;
                }
            }
        }
        
        // ✅ ORIGINAL: Costos indirectos usando la utilidad original
        double costosIndirectos = CostosIndirectosUtils.calcular("SEMANA");
        
        // ✅ ORIGINAL: Parámetros usando la lógica original pero con beneficios de ventas
        double parametros = calcularParametrosIntegrado(totalBeneficios);
        
        System.out.println("✅ Resumen calculado:");
        System.out.println("   💰 Total Beneficios (Ventas): " + totalBeneficios);
        System.out.println("   💸 Total Costos Directos (Ventas): " + totalCostosDirectos);
        System.out.println("   🏢 Costos Indirectos: " + costosIndirectos);
        System.out.println("   ⚙️ Parámetros: " + parametros);
        System.out.println("   📅 Variables del Día: " + variableDiaTotal);
        
        // Actualizar cache con todos los valores
        CacheLibroSemanal.set(Tipo.GANANCIA_B, totalBeneficios);
        CacheLibroSemanal.set(Tipo.COSTO_DIRECTO, totalCostosDirectos);
        CacheLibroSemanal.set(Tipo.COSTO_INDIRECTO, costosIndirectos);
        CacheLibroSemanal.set(Tipo.PARAMETROS, parametros);
        CacheLibroSemanal.set(Tipo.COSTOS_DIA, variableDiaTotal);
    }

    /**
     * ORIGINAL: Mantener la lógica original de cálculo de parámetros
     */
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

    public static void cargarDetallePorDia(LocalDate fechaInicio, DetallePorDiaVista vista) {
        System.out.println("📅 Cargando detalle por día desde: " + fechaInicio);
        
        // CAMBIO: Obtenemos datos de la tabla "Ventas"
        List<Map<String, String>> ventas = VerUtils.verTabla("Ventas");
        
        // Agrupar ventas por fecha
        Map<LocalDate, List<Map<String, String>>> ventasPorFecha = new HashMap<>();
        
        for (Map<String, String> venta : ventas) {
            LocalDate fecha = ParseUtils.toDate(venta.get("Fecha"));
            if (fecha != null) {
                LocalDate fechaFin = fechaInicio.plusDays(6);
                if (!fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin)) {
                    ventasPorFecha.computeIfAbsent(fecha, k -> new ArrayList<>()).add(venta);
                }
            }
        }
        
        // Configurar tablas para cada día
        for (Map.Entry<LocalDate, List<Map<String, String>>> entry : ventasPorFecha.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<Map<String, String>> ventasDelDia = entry.getValue();
            
            TableView<Map<String, String>> tabla = vista.getTablaPorFecha(fecha);
            if (tabla != null) {
                configurarColumnasVentas(tabla);
                cargarDatosVentas(tabla, ventasDelDia);
                vista.mostrarSiTieneDatos(fecha);
            }
        }
    }
    
    /**
     * NUEVO: Configura las columnas para mostrar datos de ventas
     */
    private static void configurarColumnasVentas(TableView<Map<String, String>> tabla) {
        if (!tabla.getColumns().isEmpty()) {
            return; // Ya están configuradas
        }
        
        tabla.getColumns().clear();
        
        // CAMBIO: Columnas adaptadas para la tabla de Ventas usando custom CellValueFactory
        TableColumn<Map<String, String>, String> colCodigo = new TableColumn<>("Código Venta");
        colCodigo.setCellValueFactory(crearCellValueFactory("Código Venta"));
        colCodigo.setPrefWidth(120);
        
        TableColumn<Map<String, String>, String> colCodigoProduccion = new TableColumn<>("Código Producción");
        colCodigoProduccion.setCellValueFactory(crearCellValueFactory("Código Producción"));
        colCodigoProduccion.setPrefWidth(140);
        
        TableColumn<Map<String, String>, String> colCantidad = new TableColumn<>("Cantidad Vendida");
        colCantidad.setCellValueFactory(crearCellValueFactory("Cantidad Vendida"));
        colCantidad.setPrefWidth(120);
        
        // CAMBIO: Ahora mostramos "Precio de Venta por Unidad" en lugar de otro precio
        TableColumn<Map<String, String>, String> colPrecio = new TableColumn<>("Precio de Venta por Unidad");
        colPrecio.setCellValueFactory(crearCellValueFactory("Precio de Venta por Unidad"));
        colPrecio.setPrefWidth(160);
        
        TableColumn<Map<String, String>, String> colCostoDirecto = new TableColumn<>("Costo Directo/U");
        colCostoDirecto.setCellValueFactory(crearCellValueFactory("Costo Directo/U"));
        colCostoDirecto.setPrefWidth(120);
        
        TableColumn<Map<String, String>, String> colCostoTotal = new TableColumn<>("Costo Total");
        colCostoTotal.setCellValueFactory(crearCellValueFactory("Costo Total"));
        colCostoTotal.setPrefWidth(100);
        
        // CAMBIO: Mostramos "Beneficio" en lugar de "Ganancia Total"
        TableColumn<Map<String, String>, String> colBeneficio = new TableColumn<>("Beneficio");
        colBeneficio.setCellValueFactory(crearCellValueFactory("Beneficio"));
        colBeneficio.setPrefWidth(100);
        
        // Aplicar estilos a todas las columnas
        TableCellSwitchEstiloFactory cellFactory = new TableCellSwitchEstiloFactory();
        colCodigo.setCellFactory(cellFactory);
        colCodigoProduccion.setCellFactory(cellFactory);
        colCantidad.setCellFactory(cellFactory);
        colPrecio.setCellFactory(cellFactory);
        colCostoDirecto.setCellFactory(cellFactory);
        colCostoTotal.setCellFactory(cellFactory);
        colBeneficio.setCellFactory(cellFactory);
        
        tabla.getColumns().addAll(colCodigo, colCodigoProduccion, colCantidad, 
                                colPrecio, colCostoDirecto, colCostoTotal, colBeneficio);
    }
    
    /**
     * NUEVO: Crea un CellValueFactory personalizado para Map<String, String>
     */
    private static Callback<TableColumn.CellDataFeatures<Map<String, String>, String>, ObservableValue<String>> 
            crearCellValueFactory(String key) {
        return param -> {
            Map<String, String> rowData = param.getValue();
            String value = rowData != null ? rowData.getOrDefault(key, "") : "";
            return new SimpleStringProperty(value);
        };
    }
    
    /**
     * NUEVO: Carga los datos de ventas en la tabla, calculando el costo total
     */
    private static void cargarDatosVentas(TableView<Map<String, String>> tabla, 
                                        List<Map<String, String>> ventasDelDia) {
        tabla.getItems().clear();
        
        for (Map<String, String> venta : ventasDelDia) {
            // CAMBIO: Crear mapa con datos de venta, agregando costo total calculado
            Map<String, String> filaVenta = new HashMap<>();
            filaVenta.put("Código Venta", venta.getOrDefault("Código Venta", ""));
            filaVenta.put("Código Producción", venta.getOrDefault("Código Producción", ""));
            filaVenta.put("Cantidad Vendida", venta.getOrDefault("Cantidad Vendida", "0"));
            filaVenta.put("Precio de Venta por Unidad", venta.getOrDefault("Precio de Venta por Unidad", "0"));
            filaVenta.put("Costo Directo/U", venta.getOrDefault("Costo Directo/U", "0"));
            filaVenta.put("Beneficio", venta.getOrDefault("Beneficio", "0"));
            
            // NUEVO: Calcular y agregar costo total
            double cantidadVendida = ParseUtils.toDouble(venta.getOrDefault("Cantidad Vendida", "0"));
            double costoDirectoUnidad = ParseUtils.toDouble(venta.getOrDefault("Costo Directo/U", "0"));
            double costoTotal = cantidadVendida * costoDirectoUnidad;
            filaVenta.put("Costo Total", String.format("%.2f", costoTotal));
            
            tabla.getItems().add(filaVenta);
        }
        
        System.out.println("📊 Cargadas " + tabla.getItems().size() + " ventas del día");
    }
}