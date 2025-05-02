package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioModificar;
import com.panaderiafx.controllers.components.forms.FormularioUtils;
import com.panaderiafx.controllers.components.forms.TablaBusquedaRegistros;
import com.panaderiafx.controllers.components.forms.TablaBusquedaValores;
import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class ModificarController {

    public static ScrollPane mostrar(String tabla) {
        return mostrar(tabla, tabla);
    }

    public static ScrollPane mostrar(String tabla, String nombreVisible) {
        VBox contenedorGeneral = new VBox(20);
        contenedorGeneral.setPadding(new Insets(20));
        contenedorGeneral.setStyle("-fx-background-color: #FFF3E0;");

        Label tituloGeneral = new Label("Modificar - " + nombreVisible);
        tituloGeneral.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        tituloGeneral.setAlignment(Pos.CENTER);
        tituloGeneral.setMaxWidth(Double.MAX_VALUE);

        HBox root = new HBox(30);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPrefWidth(400);
        panelIzquierdo.setAlignment(Pos.TOP_CENTER);

        Label tituloIzq = new Label("Registros disponibles");
        tituloIzq.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        panelIzquierdo.getChildren().add(tituloIzq);

        VBox panelFormulario = new VBox(10);
        panelFormulario.setPrefWidth(600);

        Label tituloCentro = new Label("Formulario de modificación");
        tituloCentro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloCentro.setVisible(false);
        panelFormulario.getChildren().add(tituloCentro);

        VBox panelDerecho = new VBox(10);
        panelDerecho.setPrefWidth(400);

        Label tituloDerecha = new Label("Selección de valores");
        tituloDerecha.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tituloDerecha.setVisible(false);
        panelDerecho.getChildren().add(tituloDerecha);

        List<Map<String, Object>> definicionCampos = FormularioUtils.generarInstrucciones(tabla);

        List<Map<String, String>> registros = VerUtils.verTabla(tabla);
        Node tablaBusqueda = TablaBusquedaRegistros.crear(registros, Math.max(2, registros.get(0).size()));
        panelIzquierdo.getChildren().add(tablaBusqueda);

        if (tablaBusqueda instanceof VBox contenedor) {
            for (Node hijo : contenedor.getChildren()) {
                if (hijo instanceof ScrollPane sp && sp.getContent() instanceof TableView tablaReal) {
                    tablaReal.setRowFactory(tv -> {
                        TableRow<Map<String, String>> row = new TableRow<>();
                        row.setOnMousePressed(event -> {
                            if (!row.isEmpty()) {
                                Map<String, String> seleccionParcial = row.getItem();
                                if (seleccionParcial == null || seleccionParcial.isEmpty()) return;

                                // 🔄 Forzar recarga del Excel
                                List<Map<String, String>> datosActualizados = VerUtils.verTabla(tabla);

                                Map<String, String> filaActual = datosActualizados.stream()
                                    .filter(m -> seleccionParcial.entrySet().stream()
                                        .allMatch(e -> Objects.equals(e.getValue(), m.get(e.getKey()))))
                                    .findFirst()
                                    .orElse(null);

                                if (filaActual != null) {
                                    panelFormulario.getChildren().removeIf(n -> n instanceof FormularioModificar);
                                    tituloCentro.setVisible(true);

                                    FormularioModificar formulario = new FormularioModificar(tabla, definicionCampos, datosActualizados, filaActual);
                                    panelFormulario.getChildren().add(formulario);

                                    formulario.getCampos().values().forEach(campo -> {
                                        if (campo instanceof CampoSeleccionExtendido campoExtendido) {
                                            campoExtendido.setOnSeleccionarListener((columnasMostrarTexto, campoExtendido2) -> {
                                                tituloDerecha.setVisible(true);
                                                panelDerecho.getChildren().removeIf(n -> n instanceof VBox || n instanceof ScrollPane);
                                                Node tablaBusquedaValores = TablaBusquedaValores.crear(campoExtendido2, columnasMostrarTexto);
                                                panelDerecho.getChildren().add(tablaBusquedaValores);
                                            });
                                        }
                                    });
                                }
                            }
                        });
                        return row;
                    });
                }
            }
        }

        root.getChildren().addAll(panelIzquierdo, panelFormulario, panelDerecho);
        contenedorGeneral.getChildren().addAll(tituloGeneral, root);

        ScrollPane sc = new ScrollPane(contenedorGeneral);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }
}
