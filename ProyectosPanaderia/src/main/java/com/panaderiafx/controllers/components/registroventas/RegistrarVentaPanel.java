package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Panel registrar ventas con estadísticas simplificadas, generador de códigos y filtro por tiempo de vida
 */
public class RegistrarVentaPanel {
    
    private final VentasService ventasService;
    private DatePicker dateFecha;
    private TextField txtPrecioVenta;
    private TextField txtCantidadVendida;
    private TextField txtProducto;
    private TextField txtCodigoProduccion;
    private TextField txtTiempoVida; // NUEVO: Campo tiempo de vida
    private TablaVentasComponent tablaVentas;
    private TablaProduccionComponent tablaProduccion;
    private EstadisticasSimplificadas estadisticas;
    
    public RegistrarVentaPanel(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        HBox contenedorPrincipal = new HBox(15);
        contenedorPrincipal.setPadding(new Insets(20));
        contenedorPrincipal.setStyle("-fx-background-color: #FF8A50;");
        contenedorPrincipal.setPrefWidth(1300);
        
        // 4 paneles con anchos equilibrados
        VBox panelTablaVentas = crearPanelTablaVentas();
        VBox panelFormulario = crearPanelFormulario();
        VBox panelTablaProduccion = crearPanelTablaProduccion();
        VBox panelEstadisticas = crearPanelEstadisticas();
        
        panelTablaVentas.setPrefWidth(500);
        panelFormulario.setPrefWidth(300);
        panelTablaProduccion.setPrefWidth(550); // Ampliado para nueva columna
        panelEstadisticas.setPrefWidth(300);
        
        contenedorPrincipal.getChildren().addAll(
            panelTablaVentas, panelFormulario, panelTablaProduccion, panelEstadisticas
        );
        
        return contenedorPrincipal;
    }
    
    private VBox crearPanelTablaVentas() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Seleccionar Venta");
        tablaVentas = new TablaVentasComponent(ventasService);
        Node tabla = tablaVentas.crear();
        
        // Botón actualizar
        Button btnActualizar = ComponentesUI.crearBotonSecundario("🔄 Actualizar");
        btnActualizar.setPrefWidth(200);
        btnActualizar.setOnAction(e -> actualizarTablas());
        
        panel.getChildren().addAll(titulo, tabla, btnActualizar);
        return panel;
    }
    
    private VBox crearPanelFormulario() {
        VBox panel = new VBox(8);
        
        // Etiquetas de sección
        Label lblVenta = ComponentesUI.crearEtiquetaTitulo("Venta");
        
        // NUEVO: Campo Tiempo de Vida
        Label lblTiempoVida = ComponentesUI.crearEtiquetaTitulo("Tiempo de Vida (días)");
        txtTiempoVida = ComponentesUI.crearCampoNumerico("3"); // Valor por defecto: 3 días
        txtTiempoVida.setPrefWidth(270);
        
        // Listener para filtrar producciones cuando cambie el tiempo de vida
        txtTiempoVida.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrarProduccionesPorTiempoVida();
        });
        
        // Fecha (único campo de fecha, editable)
        Label lblFecha = ComponentesUI.crearEtiquetaTitulo("Fecha de venta");
        dateFecha = new DatePicker(LocalDate.now());
        dateFecha.setPrefWidth(270);
        
        // Listener para filtrar producciones cuando cambie la fecha
        dateFecha.valueProperty().addListener((obs, oldVal, newVal) -> {
            filtrarProduccionesPorTiempoVida();
            // NUEVO: Actualizar fecha de venta en tabla de producción para colores
            if (newVal != null) {
                tablaProduccion.setFechaVenta(newVal);
            }
        });
        
        // Producto (NO editable, se carga automáticamente)
        Label lblProducto = ComponentesUI.crearEtiquetaTitulo("Seleccionar Producto");
        txtProducto = ComponentesUI.crearCampoTextoDeshabilitado("Sliced Bread");
        txtProducto.setPrefWidth(270);
        
        // Sección Producción
        Label lblProduccion = ComponentesUI.crearEtiquetaTitulo("Produccion");
        txtCodigoProduccion = ComponentesUI.crearCampoTextoDeshabilitado("");
        txtCodigoProduccion.setPrefWidth(270);
        
        // Precio de venta (EDITABLE)
        Label lblPrecio = ComponentesUI.crearEtiquetaTitulo("Precio de venta");
        txtPrecioVenta = ComponentesUI.crearCampoNumerico("1.85");
        txtPrecioVenta.setPrefWidth(270);
        
        // Listener para actualizar estadísticas
        txtPrecioVenta.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstadisticasEnTiempoReal();
        });
        
        // Cantidad vendida (EDITABLE)
        Label lblCantidad = ComponentesUI.crearEtiquetaTitulo("Cantidad Vendida");
        txtCantidadVendida = ComponentesUI.crearCampoNumerico("352");
        txtCantidadVendida.setPrefWidth(270);
        
        // Listener para actualizar estadísticas
        txtCantidadVendida.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstadisticasEnTiempoReal();
        });
        
        // Botón guardar
        Button btnGuardar = ComponentesUI.crearBotonPrimario("💾 Guardar");
        btnGuardar.setPrefWidth(270);
        btnGuardar.setOnAction(e -> manejarGuardarVenta());
        
        panel.getChildren().addAll(
            lblVenta,
            lblTiempoVida, txtTiempoVida, // NUEVO: Campo tiempo de vida
            lblFecha, dateFecha,
            lblProducto, txtProducto,
            lblProduccion, txtCodigoProduccion,
            lblPrecio, txtPrecioVenta,
            lblCantidad, txtCantidadVendida,
            btnGuardar
        );
        
        return panel;
    }
    
    private VBox crearPanelTablaProduccion() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Seleccionar Producción");
        tablaProduccion = new TablaProduccionComponent(ventasService);
        
        // Configurar callback para carga automática
        tablaProduccion.setOnSelectionChange(this::manejarSeleccionProduccion);
        
        Node tabla = tablaProduccion.crear();
        panel.getChildren().addAll(titulo, tabla);
        
        return panel;
    }
    
    private VBox crearPanelEstadisticas() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Estadísticas");
        estadisticas = new EstadisticasSimplificadas();
        Node estadisticasNode = estadisticas.crear();
        
        panel.getChildren().addAll(titulo, estadisticasNode);
        return panel;
    }
    
    /**
     * NUEVO: Método para filtrar producciones por tiempo de vida
     */
    private void filtrarProduccionesPorTiempoVida() {
        try {
            int tiempoVida = Integer.parseInt(txtTiempoVida.getText().trim());
            LocalDate fechaVenta = dateFecha.getValue();
            
            if (fechaVenta != null && tiempoVida > 0) {
                // Calcular rango de fechas
                LocalDate fechaInicio = fechaVenta.minusDays(tiempoVida);
                LocalDate fechaFin = fechaVenta.plusDays(tiempoVida);
                
                System.out.println("🗓️ Filtrando producciones por tiempo de vida:");
                System.out.println("   Fecha de venta: " + fechaVenta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                System.out.println("   Tiempo de vida: " + tiempoVida + " días");
                System.out.println("   Rango: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                 " al " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                // Aplicar filtro en la tabla de producción
                tablaProduccion.aplicarFiltroTiempoVida(fechaInicio, fechaFin);
            }
        } catch (NumberFormatException e) {
            // Si hay error en el número, mostrar todas las producciones
            tablaProduccion.mostrarTodasLasProducciones();
        }
    }
    
    private void manejarSeleccionProduccion() {
        TablaProduccionComponent.ProduccionRow seleccionada = tablaProduccion.getSeleccionado();
        if (seleccionada != null) {
            // CARGA AUTOMÁTICA de datos (SIN CAMBIAR LA FECHA DE VENTA)
            txtCodigoProduccion.setText(seleccionada.getCodigoProduccion());
            txtProducto.setText(seleccionada.getProducto());
            
            // ELIMINADO: NO cambiar la fecha de venta
            // La fecha de venta debe permanecer como está (fecha actual o fecha seleccionada por el usuario)
            // NO hacer: dateFecha.setValue(LocalDate.of(anio, mes, dia));
            
            // Cargar precio y cantidad
            String precioVenta = obtenerPrecioVentaDeProduccion(seleccionada.getCodigoProduccion());
            if (precioVenta != null && !precioVenta.isEmpty()) {
                txtPrecioVenta.setText(precioVenta);
            }
            
            txtCantidadVendida.setText(seleccionada.getCantidadProducida());
            
            // Actualizar estadísticas
            actualizarEstadisticasEnTiempoReal();
            
            System.out.println("✅ Producción seleccionada:");
            System.out.println("   Código: " + seleccionada.getCodigoProduccion());
            System.out.println("   Producto: " + seleccionada.getProducto());
            System.out.println("   Fecha de producción: " + seleccionada.getFecha());
            System.out.println("   Fecha de venta (NO cambiada): " + dateFecha.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }
    
    private void actualizarEstadisticasEnTiempoReal() {
        try {
            double precio = Double.parseDouble(txtPrecioVenta.getText().trim());
            double cantidad = Double.parseDouble(txtCantidadVendida.getText().trim());
            
            // Validar stock en tiempo real y mostrar advertencia
            String codigoProduccion = txtCodigoProduccion.getText().trim();
            if (!codigoProduccion.isEmpty()) {
                double disponible = ventasService.obtenerCantidadDisponible(codigoProduccion);
                if (cantidad > disponible) {
                    // Cambiar color del campo cantidad a rojo si excede stock
                    txtCantidadVendida.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 5 10; " +
                                             "-fx-border-color: #F44336; -fx-border-width: 2; " +
                                             "-fx-text-fill: #F44336;");
                } else {
                    // Restaurar estilo normal
                    txtCantidadVendida.setStyle("-fx-background-color: white; -fx-padding: 5 10; " +
                                             "-fx-border-color: #ccc; -fx-border-width: 1;");
                }
            }
            
            // Obtener costo directo
            String producto = txtProducto.getText().trim();
            double costoDirecto = ventasService.obtenerCostoDirectoPorProducto(producto);
            
            // Calcular totales
            double precioTotal = precio * cantidad;
            double costoTotal = costoDirecto * cantidad;
            double diferenciaGanancia = precioTotal - costoTotal;
            
            // Actualizar estadísticas
            estadisticas.actualizar(precioTotal, costoTotal, diferenciaGanancia);
            
        } catch (NumberFormatException e) {
            // Restaurar estilo normal si hay error
            txtCantidadVendida.setStyle("-fx-background-color: white; -fx-padding: 5 10; " +
                                     "-fx-border-color: #ccc; -fx-border-width: 1;");
            estadisticas.mostrarError();
        }
    }
    
    private void actualizarTablas() {
        System.out.println("🔄 Actualizando tablas...");
        
        // Recargar datos del Excel
        com.panaderiafx.utils.VerUtils.refrescarExcel();
        
        // Actualizar tablas
        tablaVentas.actualizar();
        tablaProduccion.actualizar();
        
        // Reaplicar filtro de tiempo de vida
        filtrarProduccionesPorTiempoVida();
        
        // Actualizar estadísticas
        actualizarEstadisticasEnTiempoReal();
        
        ComponentesUI.mostrarExito("Tablas actualizadas correctamente");
    }
    
    private String obtenerPrecioVentaDeProduccion(String codigoProduccion) {
        return ventasService.buscarEnProduccion(codigoProduccion, "Precio de Venta por Unidad");
    }
    
    private void manejarGuardarVenta() {
        try {
            VentaModel venta = construirVentaDesdeFormulario();
            if (validarVenta(venta)) {
                // Validar stock disponible
                String codigoProduccion = venta.getCodigoProduccion();
                double cantidadSolicitada = venta.getCantidadVendida();
                double cantidadDisponible = ventasService.obtenerCantidadDisponible(codigoProduccion);
                
                if (cantidadSolicitada > cantidadDisponible) {
                    ComponentesUI.mostrarError(String.format(
                        "Stock insuficiente.\n" +
                        "Disponible: %.0f unidades\n" +
                        "Solicitado: %.0f unidades\n" +
                        "Máximo permitido: %.0f unidades", 
                        cantidadDisponible, cantidadSolicitada, cantidadDisponible));
                    return;
                }
                
                boolean exito = ventasService.crearVentaConCodigo(venta);
                if (exito) {
                    ComponentesUI.mostrarExito("Venta guardada exitosamente");
                    limpiarFormulario();
                    tablaVentas.actualizar();
                    filtrarProduccionesPorTiempoVida(); // Reaplicar filtro
                    actualizarEstadisticasEnTiempoReal();
                } else {
                    ComponentesUI.mostrarError("Error al guardar la venta");
                }
            }
        } catch (Exception e) {
            ComponentesUI.mostrarError("Error: " + e.getMessage());
        }
    }
    
    private VentaModel construirVentaDesdeFormulario() {
        String fecha = dateFecha.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        double precio = Double.parseDouble(txtPrecioVenta.getText().trim());
        double cantidad = Double.parseDouble(txtCantidadVendida.getText().trim());
        String producto = txtProducto.getText().trim();
        
        VentaModel venta = new VentaModel(producto, fecha, cantidad, precio);
        venta.setCodigoProduccion(txtCodigoProduccion.getText().trim());
        
        return venta;
    }
    
    private boolean validarVenta(VentaModel venta) {
        if (venta.getProducto() == null || venta.getProducto().isEmpty()) {
            ComponentesUI.mostrarError("Seleccione una producción primero");
            return false;
        }
        if (venta.getCodigoProduccion() == null || venta.getCodigoProduccion().isEmpty()) {
            ComponentesUI.mostrarError("Código de producción requerido");
            return false;
        }
        if (venta.getPrecioVenta() <= 0 || venta.getCantidadVendida() <= 0) {
            ComponentesUI.mostrarError("Precio y cantidad deben ser mayores a cero");
            return false;
        }
        return true;
    }
    
    private void limpiarFormulario() {
        txtPrecioVenta.clear();
        txtCantidadVendida.clear();
        txtCodigoProduccion.clear();
        txtProducto.clear();
        dateFecha.setValue(LocalDate.now()); // Restaurar a fecha actual
        txtTiempoVida.setText("3"); // Restaurar valor por defecto
        estadisticas.limpiar();
        filtrarProduccionesPorTiempoVida(); // Reaplicar filtro con valores por defecto
    }
}