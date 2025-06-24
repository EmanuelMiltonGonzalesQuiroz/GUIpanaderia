package com.panaderiafx;

import com.panaderiafx.controllers.CalculadoraConversion;
import com.panaderiafx.controllers.RegistroIngresos;
import com.panaderiafx.controllers.RegistroProduccion;
import com.panaderiafx.controllers.RegistroRecetas;
import com.panaderiafx.controllers.RegistroVentas;
import com.panaderiafx.controllers.LibroSemanalController;
import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class Pestanas {

    private static boolean vistaBasica = true;

    public static BorderPane crear() {
        BorderPane root = new BorderPane();

        VBox cabecera = new VBox(10);
        HBox filaSuperior = new HBox(20); // Espacio entre botón y versión
        filaSuperior.setPadding(new Insets(10));
        filaSuperior.setAlignment(Pos.CENTER_LEFT); // Alinear elementos al inicio

        Button botonCambioVista = new Button("🔁 Cambiar a vista avanzada");
        actualizarEstiloBoton(botonCambioVista, vistaBasica);

        botonCambioVista.setOnAction(e -> {
            vistaBasica = !vistaBasica;
            String texto = vistaBasica ? "🔁 Cambiar a vista avanzada" : "🔁 Cambiar a vista básica";
            botonCambioVista.setText(texto);
            actualizarEstiloBoton(botonCambioVista, vistaBasica);
            root.setCenter(generarVista(vistaBasica));
        });

        Button botonVerReportes = new Button("📊 Ver Reportes");
        botonVerReportes.setStyle("-fx-background-color:rgb(251, 255, 0); -fx-padding: 6 12; -fx-font-weight: bold;");
        botonVerReportes.setOnAction(e -> {
            try {
                new ProcessBuilder("cmd", "/c", "start", "Datos\\dashboard.pbix").start();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alerta = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el reporte.");
                alerta.showAndWait();
            }
        });


        Label labelVersion = new Label("Versión: 1.1.0");
        labelVersion.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        filaSuperior.getChildren().addAll(botonCambioVista, espaciador,botonVerReportes, labelVersion);
        cabecera.getChildren().add(filaSuperior);

        root.setTop(cabecera);
        root.setCenter(generarVista(vistaBasica));

        return root;
    }

    private static void actualizarEstiloBoton(Button boton, boolean esVistaBasica) {
        if (esVistaBasica) {
            boton.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white; -fx-padding: 6 12; -fx-font-weight: bold;");
        } else {
            boton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 6 12; -fx-font-weight: bold;");
        }
    }

    /** Envuelve cualquier Node dentro de un ScrollPane con scroll horizontal */
    private static ScrollPane wrapHorizontalScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        return sp;
    }

    private static TabPane generarVista(boolean vistaBasica) {
        TabPane tabPane = new TabPane();
        Set<String> agregados = new HashSet<>();

        // TAB 1: Registro de Producción
        Tab produccionResumen = new Tab("Registro de Producción");
        produccionResumen.setClosable(false);
        produccionResumen.setStyle("-fx-font-size: 16px;");
        produccionResumen.setContent(wrapHorizontalScroll(RegistroProduccion.crearVista()));
        tabPane.getTabs().add(produccionResumen);

        // TAB 2: Registro de Ingresos
        Tab ingresosResumen = new Tab("Edición de Producción");
        ingresosResumen.setClosable(false);
        ingresosResumen.setStyle("-fx-font-size: 16px;");
        ingresosResumen.setContent(new Label("Cargando..."));
        ingresosResumen.setOnSelectionChanged(e -> {
            if (ingresosResumen.isSelected() && ingresosResumen.getContent() instanceof Label) {
                Node vista = RegistroIngresos.crearVista();
                ingresosResumen.setContent(wrapHorizontalScroll(vista));
            }
        });
        tabPane.getTabs().add(ingresosResumen);

        // TAB 3: Registro de Ventas
        Tab registroVentas = new Tab("Gestión de Ventas");
        registroVentas.setClosable(false);
        registroVentas.setStyle("-fx-font-size: 16px;");
        registroVentas.setContent(new Label("Cargando..."));
        registroVentas.setOnSelectionChanged(e -> {
            if (registroVentas.isSelected() && registroVentas.getContent() instanceof Label) {
                Node vista = RegistroVentas.crearVista();
                registroVentas.setContent(wrapHorizontalScroll(vista));
            }
        });
        tabPane.getTabs().add(registroVentas);

        // TAB 4: Libro Semanal
        Tab libroSemanal = new Tab("Libro Semanal");
        libroSemanal.setClosable(false);
        libroSemanal.setStyle("-fx-font-size: 16px;");
        libroSemanal.setContent(new Label("Cargando..."));
        libroSemanal.setOnSelectionChanged(e -> {
            if (libroSemanal.isSelected() && libroSemanal.getContent() instanceof Label) {
                Node vista = LibroSemanalController.crearVista();
                libroSemanal.setContent(wrapHorizontalScroll(vista));
            }
        });
        tabPane.getTabs().add(libroSemanal);

        // TAB 5: Registro de Recetas
        Tab recetasTab = new Tab("Registro de Recetas");
        recetasTab.setClosable(false);
        recetasTab.setStyle("-fx-font-size: 16px;");
        recetasTab.setContent(new Label("Cargando..."));
        recetasTab.setOnSelectionChanged(e -> {
            if (recetasTab.isSelected() && recetasTab.getContent() instanceof Label) {
                Node vista = RegistroRecetas.crearVista();
                recetasTab.setContent(wrapHorizontalScroll(vista));
            }
        });
        tabPane.getTabs().add(recetasTab);

        // Configuración dinámica
        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónTablas");
        for (Map<String, String> fila : config) {
            String nombreSistema = fila.get("Tabla");
            String nombreVisible = fila.getOrDefault("Nombre Visible", nombreSistema);
            String mostrar = fila.getOrDefault("Mostrar", "No");
            String basica = fila.getOrDefault("Vista basica", "No");

            boolean incluir = mostrar.equalsIgnoreCase("Sí") &&
                    (!vistaBasica || basica.equalsIgnoreCase("Sí"));
            if (incluir) {
                Tab tab = new Tab(nombreVisible);
                tab.setClosable(false);
                tab.setStyle("-fx-font-size: 16px;");
                tab.setContent(new Label("Cargando..."));
                tab.setOnSelectionChanged(e -> {
                    if (tab.isSelected() && tab.getContent() instanceof Label) {
                        Node subTabs = SubPestanasFactory.crear(nombreSistema, nombreVisible, vistaBasica);
                        tab.setContent(wrapHorizontalScroll(subTabs));
                    }
                });
                tabPane.getTabs().add(tab);
                agregados.add(nombreSistema.toLowerCase());
            }
        }

        // Tablas adicionales si vista avanzada
        if (!vistaBasica) {
            List<String> hojas = VerUtils.obtenerNombresTablas();
            for (String hoja : hojas) {
                if (!agregados.contains(hoja.toLowerCase())) {
                    Tab tab = new Tab(hoja);
                    tab.setClosable(false);
                    tab.setStyle("-fx-font-size: 16px;");
                    tab.setContent(new Label("Cargando..."));
                    tab.setOnSelectionChanged(e -> {
                        if (tab.isSelected() && tab.getContent() instanceof Label) {
                            Node subTabs = SubPestanasFactory.crear(hoja, hoja, false);
                            tab.setContent(wrapHorizontalScroll(subTabs));
                        }
                    });
                    tabPane.getTabs().add(tab);
                }
            }
        }

        // TAB final: Calculadora de Conversión
        Tab calculadora = new Tab("Calculadora de Conversión");
        calculadora.setClosable(false);
        calculadora.setStyle("-fx-font-size: 16px;");
        calculadora.setContent(new Label("Cargando..."));
        calculadora.setOnSelectionChanged(e -> {
            if (calculadora.isSelected() && calculadora.getContent() instanceof Label) {
                Node vista = CalculadoraConversion.crearVista();
                calculadora.setContent(wrapHorizontalScroll(vista));
            }
        });
        tabPane.getTabs().add(calculadora);

        return tabPane;
    }
}
