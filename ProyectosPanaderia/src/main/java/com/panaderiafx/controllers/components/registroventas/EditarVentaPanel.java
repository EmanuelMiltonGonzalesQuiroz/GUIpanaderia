package com.panaderiafx.controllers.components.registroventas;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

/**
 * Panel para editar ventas con estadísticas simplificadas
 */
public class EditarVentaPanel {
    
    private final VentasService ventasService;
    private TablaVentasComponent tablaVentas;
    private TextField txtCodigoProduccion;
    private TextField txtFechaVenta;
    private TextField txtPrecioVenta;
    private TextField txtCantidadVendida;
    private VentaModel ventaSeleccionada;
    private EstadisticasSimplificadas estadisticas;
    
    public EditarVentaPanel(VentasService ventasService) {
        this.ventasService = ventasService;
    }
    
    public Node crear() {
        HBox contenedor = new HBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FF8A50;");
        contenedor.setPrefWidth(1200);
        
        VBox panelTabla = crearPanelSeleccion();
        VBox panelFormulario = crearPanelEdicion();
        VBox panelEstadisticas = crearPanelEstadisticas();
        
        panelTabla.setPrefWidth(500);
        panelFormulario.setPrefWidth(350);
        panelEstadisticas.setPrefWidth(300);
        
        contenedor.getChildren().addAll(panelTabla, panelFormulario, panelEstadisticas);
        return contenedor;
    }
    
    private VBox crearPanelSeleccion() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Seleccionar Venta");
        
        tablaVentas = new TablaVentasComponent(ventasService);
        tablaVentas.setOnSelectionChange(this::manejarSeleccionVenta);
        Node tabla = tablaVentas.crear();
        
        // Botón actualizar
        Button btnActualizar = ComponentesUI.crearBotonSecundario("🔄 Actualizar");
        btnActualizar.setPrefWidth(200);
        btnActualizar.setOnAction(e -> actualizarTabla());
        
        panel.getChildren().addAll(titulo, tabla, btnActualizar);
        return panel;
    }
    
    private VBox crearPanelEdicion() {
        VBox panel = new VBox(15);
        
        // Etiquetas de sección
        Label lblVenta = ComponentesUI.crearEtiquetaTitulo("Venta");
        Label lblProduccion = ComponentesUI.crearEtiquetaTitulo("Produccion");
        
        // Campos del formulario
        txtCodigoProduccion = ComponentesUI.crearCampoTextoDeshabilitado("PRO0001");
        txtCodigoProduccion.setPrefWidth(320);
        
        VBox seccionFecha = crearSeccionFecha();
        VBox seccionPrecio = crearSeccionPrecio();
        VBox seccionCantidad = crearSeccionCantidad();
        
        // Botones de acción
        HBox botones = crearBotones();
        
        panel.getChildren().addAll(
            lblVenta, lblProduccion, txtCodigoProduccion,
            seccionFecha, seccionPrecio, seccionCantidad, botones
        );
        
        return panel;
    }
    
    private VBox crearPanelEstadisticas() {
        VBox panel = new VBox(10);
        
        Label titulo = ComponentesUI.crearEtiquetaTitulo("Diferencia al Editar");
        estadisticas = new EstadisticasSimplificadas();
        Node estadisticasNode = estadisticas.crear();
        
        panel.getChildren().addAll(titulo, estadisticasNode);
        return panel;
    }
    
    private VBox crearSeccionFecha() {
        Label lblFecha = ComponentesUI.crearEtiquetaTitulo("Fecha de venta");
        txtFechaVenta = ComponentesUI.crearCampoTextoDeshabilitado("10-06-25");
        txtFechaVenta.setPrefWidth(320);
        return new VBox(5, lblFecha, txtFechaVenta);
    }
    
    private VBox crearSeccionPrecio() {
        Label lblPrecio = ComponentesUI.crearEtiquetaTitulo("Precio de venta");
        txtPrecioVenta = ComponentesUI.crearCampoNumerico("1.85");
        txtPrecioVenta.setPrefWidth(320);
        
        // Listener para actualizar estadísticas
        txtPrecioVenta.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstadisticasDinamicas();
        });
        
        return new VBox(5, lblPrecio, txtPrecioVenta);
    }
    
    private VBox crearSeccionCantidad() {
        Label lblCantidad = ComponentesUI.crearEtiquetaTitulo("Cantidad Vendida");
        txtCantidadVendida = ComponentesUI.crearCampoNumerico("352");
        txtCantidadVendida.setPrefWidth(320);
        
        // Listener para actualizar estadísticas
        txtCantidadVendida.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstadisticasDinamicas();
        });
        
        return new VBox(5, lblCantidad, txtCantidadVendida);
    }
    
    private HBox crearBotones() {
        Button btnEditar = ComponentesUI.crearBotonSecundario("✏️ Editar");
        Button btnEliminar = ComponentesUI.crearBotonPeligro("🗑️ Eliminar");
        
        btnEditar.setPrefWidth(150);
        btnEliminar.setPrefWidth(150);
        
        btnEditar.setOnAction(e -> manejarEditar());
        btnEliminar.setOnAction(e -> manejarEliminar());
        
        HBox botones = new HBox(10, btnEditar, btnEliminar);
        return botones;
    }
    
    private void manejarSeleccionVenta() {
        TablaVentasComponent.VentaRow filaSeleccionada = tablaVentas.getSeleccionado();
        if (filaSeleccionada != null) {
            ventaSeleccionada = filaSeleccionada.getVentaOriginal();
            cargarDatosEnFormulario(ventaSeleccionada);
            actualizarEstadisticasDinamicas();
        }
    }
    
    private void cargarDatosEnFormulario(VentaModel venta) {
        txtCodigoProduccion.setText(venta.getCodigoProduccion());
        txtFechaVenta.setText(venta.getFecha());
        txtPrecioVenta.setText(String.format("%.4f", venta.getPrecioVenta()));
        txtCantidadVendida.setText(String.format("%.0f", venta.getCantidadVendida()));
    }
    
    private void actualizarEstadisticasDinamicas() {
        if (ventaSeleccionada == null || estadisticas == null) {
            return;
        }
        
        try {
            // Valores actuales del formulario
            double nuevoPrecio = Double.parseDouble(txtPrecioVenta.getText().trim());
            double nuevaCantidad = Double.parseDouble(txtCantidadVendida.getText().trim());
            
            // Validar stock en tiempo real para edición
            String codigoProduccion = ventaSeleccionada.getCodigoProduccion();
            String codigoVenta = ventaSeleccionada.getCodigoVenta();
            double disponible = ventasService.obtenerCantidadDisponibleParaEdicion(codigoProduccion, codigoVenta);
            
            if (nuevaCantidad > disponible) {
                // Cambiar color del campo cantidad a rojo si excede stock
                txtCantidadVendida.setStyle("-fx-background-color: #FFEBEE; -fx-padding: 5 10; " +
                                         "-fx-border-color: #F44336; -fx-border-width: 2; " +
                                         "-fx-text-fill: #F44336; -fx-pref-width: 320;");
            } else {
                // Restaurar estilo normal
                txtCantidadVendida.setStyle("-fx-background-color: white; -fx-padding: 5 10; " +
                                         "-fx-border-color: #ccc; -fx-border-width: 1; " +
                                         "-fx-pref-width: 320;");
            }
            
            // Valores originales
            double precioOriginal = ventaSeleccionada.getPrecioVenta();
            double cantidadOriginal = ventaSeleccionada.getCantidadVendida();
            double costoDirecto = ventaSeleccionada.getCostoDirecto();
            
            // Calcular totales originales
            double precioTotalOriginal = precioOriginal * cantidadOriginal;
            double costoTotalOriginal = costoDirecto * cantidadOriginal;
            
            // Calcular totales nuevos
            double precioTotalNuevo = nuevoPrecio * nuevaCantidad;
            double costoTotalNuevo = costoDirecto * nuevaCantidad;
            
            // Calcular diferencias
            double diferenciaPrecio = precioTotalNuevo - precioTotalOriginal;
            double diferenciaCosto = costoTotalNuevo - costoTotalOriginal;
            double diferenciaGanancia = diferenciaPrecio - diferenciaCosto;
            
            // Actualizar estadísticas (mostrar solo las diferencias)
            estadisticas.actualizar(diferenciaPrecio, diferenciaCosto, diferenciaGanancia);
            
        } catch (NumberFormatException e) {
            // Restaurar estilo normal si hay error
            txtCantidadVendida.setStyle("-fx-background-color: white; -fx-padding: 5 10; " +
                                     "-fx-border-color: #ccc; -fx-border-width: 1; " +
                                     "-fx-pref-width: 320;");
            estadisticas.mostrarError();
        }
    }
    
    private void actualizarTabla() {
        System.out.println("🔄 Actualizando tabla de ventas...");
        
        // Recargar datos del Excel
        com.panaderiafx.utils.VerUtils.refrescarExcel();
        
        // Actualizar tabla
        tablaVentas.actualizar();
        
        ComponentesUI.mostrarExito("Tabla actualizada correctamente");
    }
    
    private void manejarEditar() {
        if (ventaSeleccionada == null) {
            ComponentesUI.mostrarError("Seleccione una venta para editar");
            return;
        }
        
        try {
            double nuevoPrecio = Double.parseDouble(txtPrecioVenta.getText().trim());
            double nuevaCantidad = Double.parseDouble(txtCantidadVendida.getText().trim());
            
            if (nuevoPrecio <= 0 || nuevaCantidad <= 0) {
                ComponentesUI.mostrarError("Precio y cantidad deben ser mayores a cero");
                return;
            }
            
            // Validar stock disponible para edición
            String codigoProduccion = ventaSeleccionada.getCodigoProduccion();
            String codigoVenta = ventaSeleccionada.getCodigoVenta();
            double cantidadDisponible = ventasService.obtenerCantidadDisponibleParaEdicion(codigoProduccion, codigoVenta);
            
            if (nuevaCantidad > cantidadDisponible) {
                ComponentesUI.mostrarError(String.format(
                    "Stock insuficiente para editar.\n" +
                    "Disponible: %.0f unidades\n" +
                    "Solicitado: %.0f unidades\n" +
                    "Máximo permitido: %.0f unidades", 
                    cantidadDisponible, nuevaCantidad, cantidadDisponible));
                return;
            }
            
            VentaModel ventaActualizada = crearVentaActualizada(nuevoPrecio, nuevaCantidad);
            boolean exito = ventasService.actualizarVenta(ventaSeleccionada.getCodigoVenta(), ventaActualizada);
            
            if (exito) {
                ComponentesUI.mostrarExito("Venta actualizada exitosamente");
                tablaVentas.actualizar();
                // Recargar la venta actualizada
                manejarSeleccionVenta();
            } else {
                ComponentesUI.mostrarError("Error al actualizar la venta");
            }
            
        } catch (NumberFormatException e) {
            ComponentesUI.mostrarError("Ingrese valores numéricos válidos");
        }
    }
    
    private void manejarEliminar() {
        if (ventaSeleccionada == null) {
            ComponentesUI.mostrarError("Seleccione una venta para eliminar");
            return;
        }
        
        boolean confirmado = ComponentesUI.mostrarConfirmacion(
            "¿Está seguro de eliminar esta venta?\n" +
            "Producto: " + ventaSeleccionada.getProducto() + "\n" +
            "Fecha: " + ventaSeleccionada.getFecha()
        );
        
        if (confirmado) {
            boolean exito = ventasService.eliminarVenta(ventaSeleccionada.getCodigoVenta());
            if (exito) {
                ComponentesUI.mostrarExito("Venta eliminada exitosamente");
                tablaVentas.actualizar();
                limpiarFormulario();
                estadisticas.limpiar();
            } else {
                ComponentesUI.mostrarError("Error al eliminar la venta");
            }
        }
    }
    
    private VentaModel crearVentaActualizada(double precio, double cantidad) {
        VentaModel venta = new VentaModel();
        venta.setPrecioVenta(precio);
        venta.setCantidadVendida(cantidad);
        return venta;
    }
    
    private void limpiarFormulario() {
        txtCodigoProduccion.clear();
        txtFechaVenta.clear();
        txtPrecioVenta.clear();
        txtCantidadVendida.clear();
        ventaSeleccionada = null;
    }
}