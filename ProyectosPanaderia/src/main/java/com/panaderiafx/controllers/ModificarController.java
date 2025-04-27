package com.panaderiafx.controllers;

import com.panaderiafx.controllers.components.CampoSeleccionExtendido;
import com.panaderiafx.controllers.components.FormularioModificar;
import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

public class ModificarController {

    private static final int CANTIDAD_COLUMNAS_BUSQUEDA = 2; // 🛠 Cambia este valor para más o menos columnas

    public static ScrollPane mostrar(String tabla) {
        return mostrar(tabla, tabla);
    }

    public static ScrollPane mostrar(String tabla, String nombreVisible) {
        List<Map<String, String>> registros = VerUtils.verTabla(tabla);
        if (registros.isEmpty()) {
            VBox vacio = new VBox(new Label("No hay datos disponibles para modificar en: " + nombreVisible));
            vacio.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return new ScrollPane(vacio);
        }

        List<Map<String, Object>> definicionCampos = generarInstrucciones(tabla, registros.get(0));

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
        panelIzquierdo.getChildren().addAll(tituloIzq);

        ScrollPane scrollTabla = new ScrollPane();
        scrollTabla.setFitToWidth(true);
        scrollTabla.setFitToHeight(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 🛠 Activar scroll horizontal
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        TableView<Map<String, String>> tablaBusqueda = crearTablaBusqueda(registros);
        scrollTabla.setContent(tablaBusqueda);

        panelIzquierdo.getChildren().add(scrollTabla);

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

        tablaBusqueda.setOnMouseClicked(event -> {
            Map<String, String> fila = tablaBusqueda.getSelectionModel().getSelectedItem();
            if (fila != null) {
                panelFormulario.getChildren().removeIf(n -> n instanceof FormularioModificar);
                tituloCentro.setVisible(true);

                FormularioModificar formulario = new FormularioModificar(tabla, definicionCampos, registros, fila);
                panelFormulario.getChildren().add(formulario);

                formulario.getCampos().values().forEach(nodo -> {
                    if (nodo instanceof CampoSeleccionExtendido campoExtendido) {
                        campoExtendido.setOnSeleccionarListener((columnasMostrar, campo) -> {
                            tituloDerecha.setVisible(true);
                            panelDerecho.getChildren().removeIf(n -> n instanceof TableView);
                            panelDerecho.getChildren().add(crearTabla(campoExtendido, columnasMostrar));
                        });
                    }
                });
            }
        });

        root.getChildren().addAll(panelIzquierdo, panelFormulario, panelDerecho);
        contenedorGeneral.getChildren().addAll(tituloGeneral, root);

        ScrollPane sc = new ScrollPane(contenedorGeneral);
        sc.setFitToWidth(true);
        sc.setFitToHeight(true);
        return sc;
    }

    private static TableView<Map<String, String>> crearTablaBusqueda(List<Map<String, String>> registros) {
        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<String> columnas = registros.get(0).keySet().stream()
                .limit(CANTIDAD_COLUMNAS_BUSQUEDA)
                .toList();

        for (String col : columnas) {
            TableColumn<Map<String, String>, String> columna = new TableColumn<>(col);
            columna.setCellValueFactory(cellData -> {
                String valor = cellData.getValue().getOrDefault(col, "");
                return new SimpleStringProperty(valor);
            });
            tabla.getColumns().add(columna);
        }

        tabla.getItems().addAll(registros);
        return tabla;
    }

    private static Node crearTabla(CampoSeleccionExtendido campoExtendido, String columnasMostrarTexto) {
        String tablaOrigen = campoExtendido.getTabla();
        List<String> columnas = Arrays.stream(columnasMostrarTexto.split(","))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .toList();

        List<Map<String, String>> datos = VerUtils.verTabla(tablaOrigen);

        TableView<Map<String, String>> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for (String col : columnas) {
            TableColumn<Map<String, String>, String> columna = new TableColumn<>(col);
            columna.setCellValueFactory(cellData -> {
                String valor = cellData.getValue().getOrDefault(col, "");
                return new SimpleStringProperty(valor);
            });
            tabla.getColumns().add(columna);
        }

        tabla.getItems().addAll(datos);

        tabla.setOnMouseClicked(event -> {
            Map<String, String> fila = tabla.getSelectionModel().getSelectedItem();
            if (fila != null) {
                String valor = fila.getOrDefault(campoExtendido.getColumna(), "");
                campoExtendido.setValorDesdeTabla(valor);
            }
        });

        return tabla;
    }

    private static List<Map<String, Object>> generarInstrucciones(String tabla, Map<String, String> ejemplo) {
        List<Map<String, Object>> definicion = new ArrayList<>();
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónFormularios");

        for (Map<String, String> fila : config) {
            if (!fila.getOrDefault("Tabla", "").equalsIgnoreCase(tabla)) continue;

            Map<String, Object> campo = new HashMap<>();
            campo.put("nombre", fila.get("Campo"));
            campo.put("tipo", fila.get("Tipo").toLowerCase());

            if (fila.getOrDefault("Tipo", "").equalsIgnoreCase("select")) {
                campo.put("origen", fila.get("Origen"));
                campo.put("datoMostrar", fila.get("Dato a Mostrar"));
                campo.put("datoCargar", fila.get("Dato a Cargar"));
            }

            definicion.add(campo);
        }

        if (definicion.isEmpty()) {
            VBox error = new VBox(new Label("❌ No hay configuración definida para: " + tabla));
            error.setStyle("-fx-alignment: center; -fx-padding: 20px;");
            return List.of(Map.of("nombre", "ERROR", "tipo", "label"));
        }

        return definicion;
    }
}
