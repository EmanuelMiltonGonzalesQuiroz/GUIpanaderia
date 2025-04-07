package com.panaderiafx.controllers;

import javafx.scene.Node;
import javafx.scene.control.Button;
import com.panaderiafx.config.FormularioConfig;
import com.panaderiafx.utils.ValidacionUtils;
import com.panaderiafx.utils.ExcelUtils;

import java.time.LocalDate;
import java.util.*;

public class BotonModificarFactory {

    public static Button crearBotonModificar(String tabla, Map<String, String> registro, List<Node> campos) {
        return FormularioConfig.crearBoton("Modificar", () -> {
            Map<String, String> nuevosDatos = FormularioConfig.obtenerDatos(campos);

            for (Map.Entry<String, String> entry : nuevosDatos.entrySet()) {
                String campo = entry.getKey();
                String valor = entry.getValue();

                boolean esNumero = campo.equalsIgnoreCase("precio") || campo.equalsIgnoreCase("monto")
                        || campo.equalsIgnoreCase("valor") || campo.equalsIgnoreCase("factor");

                if (esNumero && !ValidacionUtils.esNumeroValido(valor)) {
                    FormularioConfig.mostrarMensaje("❌ '" + campo + "' debe ser un número válido.");
                    return;
                }
                if (!esNumero && !ValidacionUtils.esTextoValido(valor)) {
                    FormularioConfig.mostrarMensaje("❌ '" + campo + "' no puede estar vacío.");
                    return;
                }
            }

            if (tabla.equals("Ingredientes")) {
                String antiguo = registro.get("precio");
                String nuevo = nuevosDatos.get("precio");
                if (!Objects.equals(antiguo, nuevo)) {
                    Map<String, String> historial = new HashMap<>();
                    historial.put("Código", registro.get("codigo"));
                    historial.put("Ingrediente", registro.get("nombre"));
                    historial.put("Fecha", LocalDate.now().toString());
                    historial.put("Precio anterior", antiguo);
                    historial.put("Precio nuevo", nuevo);
                    ExcelUtils.guardarEnTabla("HistorialPrecios", historial);
                }
            }

            boolean exito = ExcelUtils.guardarEnTabla(tabla, nuevosDatos);
            FormularioConfig.mostrarMensaje(exito ? "✅ Modificado" : "❌ Error al modificar");
        });
    }
}
