package com.panaderiafx.controllers.components.registroproduccion2;

import com.panaderiafx.utils.VerUtils;
import com.panaderiafx.utils.componentes.ParseUtils;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.util.LinkedHashMap;
import java.util.Map;

public class BotonGuardarProduccion extends HBox {

    public BotonGuardarProduccion(PanelSelectorRecetaConTabla selector, FormularioNuevaReceta formExtra) {
        setPadding(new Insets(10));
        setSpacing(10);

        Button btnGuardar = new Button("Guardar Producción");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        btnGuardar.setOnAction(e -> {
            String codReceta = selector.getCodigoRecetaSeleccionado();
            String fecha = selector.getFechaSeleccionada();
            String nombreProducto = VerUtils.buscarPorCodigo("Recetas", "Código receta", codReceta, "Producto");

            if (codReceta == null || fecha.isEmpty()) {
                mostrarError("Debe seleccionar una receta y una fecha.");
                return;
            }

            String cantidadStr = formExtra.getCantidad();
            String precioUStr = formExtra.getPrecioUnitario();
            String totalStr = formExtra.getPrecioTotal();
            String mezcla = formExtra.getMezclas();
            String costoUnitarioStr = formExtra.getCostoUnitario();
            String costoTotalStr = formExtra.getCostoDirecto();

            if (cantidadStr.isEmpty() || precioUStr.isEmpty() || totalStr.isEmpty()) {
                mostrarError("Debe ingresar cantidad, precio por unidad y precio total.");
                return;
            }

            double cantidad, precioU, total, costoUnitario, costoTotal;

            try {
                cantidad = Double.parseDouble(cantidadStr);
                precioU = Double.parseDouble(precioUStr);
                total = Double.parseDouble(totalStr);
                costoUnitario = ParseUtils.toDouble(costoUnitarioStr);
                costoTotal = ParseUtils.toDouble(costoTotalStr);

                if (cantidad == 0 || precioU == 0 || total == 0) {
                    mostrarError("Cantidad, precio unitario y total deben ser mayores a cero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarError("❌ Error al interpretar valores numéricos:\n"
                    + "- Cantidad: " + cantidadStr + "\n"
                    + "- Precio/U: " + precioUStr + "\n"
                    + "- Total: " + totalStr + "\n"
                    + "- Costo/U: " + costoUnitarioStr + "\n"
                    + "- Costo Total: " + costoTotalStr + "\n\n"
                    + "Verifique que usen punto (.) como separador decimal, no coma (,).");
                return;
            }

            double gananciaTotal = (precioU - costoUnitario) * cantidad;

            Map<String, String> filaProduccion = new LinkedHashMap<>();
            filaProduccion.put("Código receta", codReceta);
            filaProduccion.put("Fecha", fecha);
            filaProduccion.put("Producto", nombreProducto);
            filaProduccion.put("Cantidad producida", String.format("%.2f", cantidad));
            filaProduccion.put("Precio de Venta por Unidad", String.format("%.2f", precioU));
            filaProduccion.put("Mezcla", mezcla);
            filaProduccion.put("Costo directo", String.format("%.2f", costoTotal));
            filaProduccion.put("Costo/U", String.format("%.4f", costoUnitario));
            filaProduccion.put("Costo Total", String.format("%.2f", costoTotal));
            filaProduccion.put("Ganancia Total", String.format("%.2f", gananciaTotal));

            GuardarProduccionUtils.guardar(filaProduccion, formExtra.isGuardarReceta());
        });

        getChildren().add(btnGuardar);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
