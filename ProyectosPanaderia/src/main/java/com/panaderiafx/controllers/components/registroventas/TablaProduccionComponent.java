package com.panaderiafx.controllers.components.registroventas;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Componente para mostrar tabla de producción ampliada con columna de inventario, 
 * filtro por tiempo de vida y colores basados en días de antigüedad
 */
public class TablaProduccionComponent {
    
    private final VentasService ventasService;
    private TableView<ProduccionRow> tabla;
    private Runnable onSelectionChange;
    private List<ProduccionRow> datosOriginales; // Para filtrado
    private LocalDate fechaVentaSeleccionada = LocalDate.now(); // NUEVO: Fecha de venta para calcular colores
    
    public TablaProduccionComponent(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        configurarTabla();
        cargarDatos();
        return tabla;
    }
    
    public void setOnSelectionChange(Runnable callback) {
        this.onSelectionChange = callback;
    }
    
    public ProduccionRow getSeleccionado() {
        return tabla.getSelectionModel().getSelectedItem();
    }
    
    public void actualizar() {
        cargarDatos();
    }
    
    /**
     * NUEVO: Establecer la fecha de venta para calcular colores
     */
    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVentaSeleccionada = fechaVenta;
        // Refrescar tabla para aplicar nuevos colores
        if (tabla != null) {
            tabla.refresh();
        }
        System.out.println("📅 Fecha de venta configurada: " + fechaVenta + " - Colores actualizados");
    }
    
    /**
     * CAMBIO: Verifica si un producto está en rojo (vencido o último día)
     */
    private boolean esProductoEnEstadoRojo(ProduccionRow row) {
        try {
            String fechaStr = row.getFecha();
            if (fechaStr == null || fechaStr.isEmpty()) {
                return false;
            }
            
            String[] partes = fechaStr.split("/");
            if (partes.length == 3) {
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int anio = Integer.parseInt(partes[2]);
                LocalDate fechaProduccion = LocalDate.of(anio, mes, dia);
                LocalDate fechaVencimiento = fechaProduccion.plusDays(3); // 3 días de vida útil
                
                // Es rojo si está vencido o es el último día
                return this.fechaVentaSeleccionada.isAfter(fechaVencimiento) || 
                       this.fechaVentaSeleccionada.isEqual(fechaVencimiento);
            }
        } catch (Exception e) {
            System.err.println("Error verificando estado rojo: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * NUEVO: Método para aplicar filtro por tiempo de vida
     */
    public void aplicarFiltroTiempoVida(LocalDate fechaInicio, LocalDate fechaFin) {
        if (datosOriginales == null) {
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        List<ProduccionRow> datosFiltrados = datosOriginales.stream()
                .filter(row -> {
                    try {
                        String fechaStr = row.getFecha();
                        if (fechaStr == null || fechaStr.isEmpty()) {
                            return false;
                        }
                        
                        // Parsear fecha de producción
                        String[] partes = fechaStr.split("/");
                        if (partes.length == 3) {
                            int dia = Integer.parseInt(partes[0]);
                            int mes = Integer.parseInt(partes[1]);
                            int anio = Integer.parseInt(partes[2]);
                            LocalDate fechaProduccion = LocalDate.of(anio, mes, dia);
                            
                            // Verificar si está en el rango
                            return !fechaProduccion.isBefore(fechaInicio) && !fechaProduccion.isAfter(fechaFin);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al parsear fecha: " + row.getFecha());
                    }
                    return false;
                })
                .collect(Collectors.toList());
        
        // Actualizar tabla con datos filtrados
        tabla.getItems().clear();
        tabla.getItems().addAll(datosFiltrados);
        
        System.out.println("🔍 Producciones filtradas: " + datosFiltrados.size() + " de " + datosOriginales.size());
    }
    
    /**
     * NUEVO: Método para mostrar todas las producciones (quitar filtro)
     */
    public void mostrarTodasLasProducciones() {
        if (datosOriginales != null) {
            tabla.getItems().clear();
            tabla.getItems().addAll(datosOriginales);
            System.out.println("📋 Mostrando todas las producciones: " + datosOriginales.size());
        }
    }
    
    private void configurarTabla() {
        tabla = new TableView<>();
        tabla.setPrefHeight(400);
        tabla.setPrefWidth(580); // Ampliado para nueva columna
        
        // Configurar las columnas principales con anchos optimizados
        TableColumn<ProduccionRow, String> colCodigo = crearColumna("Código", "codigoProduccion", 90);
        TableColumn<ProduccionRow, String> colFecha = crearColumna("Fecha", "fecha", 80);
        TableColumn<ProduccionRow, String> colCantidad = crearColumna("Cant. Prod.", "cantidadProducida", 90);
        TableColumn<ProduccionRow, String> colInventario = crearColumna("Inventario", "inventario", 90); // NUEVA COLUMNA
        TableColumn<ProduccionRow, String> colMezcla = crearColumna("Mezcla", "mezcla", 70);
        TableColumn<ProduccionRow, String> colProducto = crearColumna("Producto", "producto", 160);
        
        tabla.getColumns().addAll(colCodigo, colFecha, colCantidad, colInventario, colMezcla, colProducto);
        
        // Política de redimensionamiento
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // NUEVO: Configurar selección con alerta SOLO para productos en rojo
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // CAMBIO: Solo mostrar alerta si el producto está en estado rojo (más de 3 días)
                if (esProductoEnEstadoRojo(newSelection)) {
                    mostrarAlertaDias(newSelection);
                }
                
                if (onSelectionChange != null) {
                    onSelectionChange.run();
                }
            }
        });
        
        // Estilo
        tabla.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
        
        // NUEVO: Row factory para colorear filas según días de antigüedad
        tabla.setRowFactory(tv -> {
            TableRow<ProduccionRow> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    // Calcular días de antigüedad y aplicar color
                    String colorStyle = calcularColorPorDias(newItem);
                    row.setStyle(colorStyle);
                }
            });
            return row;
        });
    }
    
    /**
     * CAMBIO: Calcula el color basado en 3 días de vida útil desde fecha actual
     */
    private String calcularColorPorDias(ProduccionRow row) {
        try {
            String fechaStr = row.getFecha();
            if (fechaStr == null || fechaStr.isEmpty()) {
                return ""; // Sin color si no hay fecha
            }
            
            // Parsear fecha de producción
            String[] partes = fechaStr.split("/");
            if (partes.length == 3) {
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int anio = Integer.parseInt(partes[2]);
                LocalDate fechaProduccion = LocalDate.of(anio, mes, dia);
                
                // CAMBIO: Usar fecha actual como punto de partida (3 días de vida útil base)
                LocalDate fechaActual = LocalDate.now();
                LocalDate fechaVencimiento = fechaProduccion.plusDays(3); // 3 días de vida útil
                
                // Solo colorear si la fecha de venta es posterior al vencimiento
                if (this.fechaVentaSeleccionada.isAfter(fechaVencimiento)) {
                    // Producto vencido - Rojo intenso
                    return "-fx-background-color: #FFCDD2; -fx-border-color: #D32F2F; -fx-border-width: 2; -fx-text-fill: #B71C1C;";
                } else if (this.fechaVentaSeleccionada.isEqual(fechaVencimiento)) {
                    // Último día de vida útil - Rojo claro
                    return "-fx-background-color: #FFEBEE; -fx-border-color: #F44336; -fx-border-width: 1;";
                } else {
                    // Calcular días restantes de vida útil
                    long diasRestantes = ChronoUnit.DAYS.between(this.fechaVentaSeleccionada, fechaVencimiento);
                    
                    if (diasRestantes >= 2) {
                        // 2+ días restantes - Verde
                        return "-fx-background-color: #E8F5E8; -fx-border-color: #4CAF50; -fx-border-width: 1;";
                    } else if (diasRestantes == 1) {
                        // 1 día restante - Amarillo
                        return "-fx-background-color: #FFF8E1; -fx-border-color: #FF9800; -fx-border-width: 1;";
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculando días para colorear: " + e.getMessage());
        }
        
        return ""; // Sin color por defecto
    }
    
    /**
     * CAMBIO: Solo muestra alerta para productos en rojo (vencidos o último día)
     */
    private void mostrarAlertaDias(ProduccionRow row) {
        try {
            String fechaStr = row.getFecha();
            if (fechaStr == null || fechaStr.isEmpty()) {
                return;
            }
            
            String[] partes = fechaStr.split("/");
            if (partes.length == 3) {
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                int anio = Integer.parseInt(partes[2]);
                LocalDate fechaProduccion = LocalDate.of(anio, mes, dia);
                LocalDate fechaVencimiento = fechaProduccion.plusDays(3); // 3 días de vida útil
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String titulo, icono, mensaje;
                Alert.AlertType tipoAlerta;
                
                if (this.fechaVentaSeleccionada.isAfter(fechaVencimiento)) {
                    // Producto vencido
                    long diasVencido = ChronoUnit.DAYS.between(fechaVencimiento, this.fechaVentaSeleccionada);
                    titulo = "🔴 Producto VENCIDO";
                    icono = "❌";
                    mensaje = String.format("⚠️ PELIGRO: Este producto está VENCIDO.\n\n" +
                                          "Fecha de producción: %s\n" +
                                          "Fecha de vencimiento: %s\n" +
                                          "Fecha de venta: %s\n" +
                                          "Días vencido: %d día(s)\n\n" +
                                          "Producto: %s\n" +
                                          "Código: %s\n" +
                                          "Inventario: %s unidades\n\n" +
                                          "❌ NO RECOMENDADO PARA VENTA\n" +
                                          "⚠️ Evalúe retirar del inventario por seguridad alimentaria.", 
                                          fechaStr, fechaVencimiento.format(formatter), 
                                          this.fechaVentaSeleccionada.format(formatter), diasVencido,
                                          row.getProducto(), row.getCodigoProduccion(), row.getInventario());
                    tipoAlerta = Alert.AlertType.ERROR;
                } else if (this.fechaVentaSeleccionada.isEqual(fechaVencimiento)) {
                    // Último día de vida útil
                    titulo = "🟠 Último Día de Vida Útil";
                    icono = "⚠️";
                    mensaje = String.format("⚠️ ATENCIÓN: Este producto está en su ÚLTIMO DÍA de vida útil.\n\n" +
                                          "Fecha de producción: %s\n" +
                                          "Fecha de vencimiento: %s (HOY)\n" +
                                          "Fecha de venta: %s\n\n" +
                                          "Producto: %s\n" +
                                          "Código: %s\n" +
                                          "Inventario: %s unidades\n\n" +
                                          "⚠️ RECOMENDACIÓN:\n" +
                                          "• Aplicar descuento significativo\n" +
                                          "• Verificar calidad del producto\n" +
                                          "• Vender con prioridad\n" +
                                          "• Informar al cliente sobre la fecha", 
                                          fechaStr, fechaVencimiento.format(formatter), 
                                          this.fechaVentaSeleccionada.format(formatter),
                                          row.getProducto(), row.getCodigoProduccion(), row.getInventario());
                    tipoAlerta = Alert.AlertType.WARNING;
                } else {
                    // No mostrar alerta para productos en buen estado
                    return;
                }
                
                // Crear y mostrar alerta
                Alert alerta = new Alert(tipoAlerta);
                alerta.setTitle(titulo);
                alerta.setHeaderText(icono + " Estado de Vida Útil del Producto");
                alerta.setContentText(mensaje);
                alerta.show();
            }
        } catch (Exception e) {
            System.err.println("Error mostrando alerta de días: " + e.getMessage());
        }
    }
    
    private TableColumn<ProduccionRow, String> crearColumna(String titulo, String propiedad, double ancho) {
        TableColumn<ProduccionRow, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        columna.setPrefWidth(ancho);
        columna.setMinWidth(ancho - 10);
        columna.setMaxWidth(ancho + 30);
        
        // Alineación para columnas numéricas
        if (propiedad.equals("cantidadProducida") || propiedad.equals("mezcla") || propiedad.equals("inventario")) {
            columna.setStyle("-fx-alignment: CENTER-RIGHT;");
        }
        
        return columna;
    }
    
    private void cargarDatos() {
        tabla.getItems().clear();
        
        // Cargar datos de la tabla "Produccion" del Excel
        List<Map<String, String>> producciones = VerUtils.verTabla("Produccion");
        
        // Crear filas con cálculo de inventario
        datosOriginales = producciones.stream()
                .map(produccion -> new ProduccionRow(produccion, ventasService))
                .collect(Collectors.toList());
        
        tabla.getItems().addAll(datosOriginales);
        
        System.out.println("📊 Tabla Producción cargada con " + tabla.getItems().size() + " registros");
    }
    
    // Clase interna para la tabla de producción con inventario
    public static class ProduccionRow {
        private final String codigoProduccion;
        private final String fecha;
        private final String cantidadProducida;
        private final String inventario; // NUEVO
        private final String mezcla;
        private final String producto;
        
        public ProduccionRow(Map<String, String> produccion, VentasService ventasService) {
            this.codigoProduccion = produccion.getOrDefault("Código Producción", "");
            this.fecha = produccion.getOrDefault("Fecha", "");
            this.cantidadProducida = produccion.getOrDefault("Cantidad Producida", "");
            this.mezcla = produccion.getOrDefault("Mezcla", "");
            this.producto = produccion.getOrDefault("Producto", "");
            
            // NUEVO: Calcular inventario (Cantidad Producida - Cantidad Vendida)
            this.inventario = calcularInventario(produccion, ventasService);
        }
        
        /**
         * NUEVO: Método para calcular el inventario disponible
         */
        private String calcularInventario(Map<String, String> produccion, VentasService ventasService) {
            try {
                String codigoProd = produccion.getOrDefault("Código Producción", "");
                if (codigoProd.isEmpty()) {
                    return "0";
                }
                
                // Obtener cantidad producida
                String cantidadProdStr = produccion.getOrDefault("Cantidad Producida", "0");
                double cantidadProducida = ParseUtils.toDouble(cantidadProdStr);
                
                // Calcular total vendido para esta producción específica
                double totalVendido = ventasService.obtenerTodasLasVentas().stream()
                        .filter(venta -> codigoProd.equals(venta.getCodigoProduccion()))
                        .mapToDouble(venta -> venta.getCantidadVendida())
                        .sum();
                
                // Calcular inventario restante (nunca negativo)
                double inventarioRestante = Math.max(0, cantidadProducida - totalVendido);
                
                return String.format("%.0f", inventarioRestante);
                
            } catch (Exception e) {
                System.err.println("Error calculando inventario para " + 
                                 produccion.getOrDefault("Código Producción", "UNKNOWN") + ": " + e.getMessage());
                return "0";
            }
        }
        
        public String getCodigoProduccion() { return codigoProduccion; }
        public String getFecha() { return fecha; }
        public String getCantidadProducida() { return cantidadProducida; }
        public String getInventario() { return inventario; } // NUEVO
        public String getMezcla() { return mezcla; }
        public String getProducto() { return producto; }
    }
}