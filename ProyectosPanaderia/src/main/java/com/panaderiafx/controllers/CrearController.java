package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioDinamico;
import com.panaderiafx.controllers.components.forms.ContenedorFactory;
import com.panaderiafx.controllers.components.forms.FormularioUtils;
import com.panaderiafx.controllers.components.forms.TablaBusquedaValores;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class CrearController {

    public static ScrollPane mostrar(String tabla) {
        return mostrar(tabla, tabla);
    }

    public static ScrollPane mostrar(String tabla, String nombreVisible) {
        List<Map<String, Object>> definicionCampos = FormularioUtils.generarInstrucciones(tabla);

        VBox contenedorVertical = ContenedorFactory.crearContenedorPrincipal("Crear - " + nombreVisible);
        HBox contenedorGeneral = new HBox(30);
        contenedorGeneral.setPadding(new javafx.geometry.Insets(20));

        VBox contenedorFormulario = ContenedorFactory.crearContenedorFormulario("Formulario de creación");
        VBox contenedorTabla = ContenedorFactory.crearContenedorTabla("Selección de valores");
        Label tituloTabla = (Label) contenedorTabla.getChildren().get(0);

        FormularioDinamico formulario = new FormularioDinamico(tabla, definicionCampos);
        contenedorFormulario.getChildren().add(formulario);

        formulario.getCampos().values().forEach(nodo -> {
            if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                campoExtendido.setOnSeleccionarListener((columnasMostrarTexto, campo) -> {
                    contenedorTabla.getChildren().removeIf(n -> n instanceof VBox || n instanceof ScrollPane);
                    contenedorTabla.getChildren().add(
                        TablaBusquedaValores.crear(campo, columnasMostrarTexto)
                    );
                    tituloTabla.setVisible(true);
                });
                
            }
        });

        contenedorGeneral.getChildren().addAll(contenedorFormulario, contenedorTabla);
        contenedorVertical.getChildren().add(contenedorGeneral);

        ScrollPane sc = new ScrollPane(contenedorVertical);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }
}
