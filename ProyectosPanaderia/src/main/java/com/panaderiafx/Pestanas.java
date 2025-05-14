package com.panaderiafx;

import com.panaderiafx.controllers.CalculadoraConversion;
import com.panaderiafx.controllers.RegistroIngresos;
import com.panaderiafx.controllers.RegistroProduccion;
import com.panaderiafx.controllers.RegistroRecetas;
import com.panaderiafx.utils.VerUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class Pestanas {

    private static boolean vistaBasica = true;

    public static BorderPane crear() {
        BorderPane root = new BorderPane();

        VBox cabecera = new VBox(10);
        Button botonCambioVista = new Button("🔁 Cambiar a vista avanzada");
        actualizarEstiloBoton(botonCambioVista, vistaBasica);

        botonCambioVista.setOnAction(e -> {
            vistaBasica = !vistaBasica;
            String texto = vistaBasica ? "🔁 Cambiar a vista avanzada" : "🔁 Cambiar a vista básica";
            botonCambioVista.setText(texto);
            actualizarEstiloBoton(botonCambioVista, vistaBasica);
            root.setCenter(generarVista(vistaBasica));
        });

        cabecera.setPadding(new Insets(10));
        cabecera.getChildren().add(botonCambioVista);
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

    private static TabPane generarVista(boolean vistaBasica) {
        TabPane tabPane = new TabPane();
        Set<String> agregados = new HashSet<>();

        // TAB 1: Registro de Producción (único que se carga inmediatamente)
        Tab produccionResumen = new Tab("Registro de Producción");
        produccionResumen.setClosable(false);
        produccionResumen.setStyle("-fx-font-size: 16px;");
        produccionResumen.setContent(RegistroProduccion.crearVista()); // carga inmediata
        tabPane.getTabs().add(produccionResumen);

        // TAB 2: Registro de Ingresos (lazy)
        Tab ingresosResumen = new Tab("Registro de Ingresos");
        ingresosResumen.setClosable(false);
        ingresosResumen.setStyle("-fx-font-size: 16px;");
        ingresosResumen.setContent(new Label("Cargando..."));
        ingresosResumen.setOnSelectionChanged(e -> {
            if (ingresosResumen.isSelected() && ingresosResumen.getContent() instanceof Label) {
                ingresosResumen.setContent(RegistroIngresos.crearVista());
            }
        });
        tabPane.getTabs().add(ingresosResumen);

        // TAB 3: Registro de Recetas (lazy)
        Tab recetasTab = new Tab("Registro de Recetas");
        recetasTab.setClosable(false);
        recetasTab.setStyle("-fx-font-size: 16px;");
        recetasTab.setContent(new Label("Cargando..."));
        recetasTab.setOnSelectionChanged(e -> {
            if (recetasTab.isSelected() && recetasTab.getContent() instanceof Label) {
                recetasTab.setContent(RegistroRecetas.crearVista());
            }
        });
        tabPane.getTabs().add(recetasTab);

        // Otras pestañas desde configuración
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
                        tab.setContent(subTabs);
                    }
                });

                tabPane.getTabs().add(tab);
                agregados.add(nombreSistema.toLowerCase());
            }
        }

        // Pestañas restantes si es vista avanzada
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
                            tab.setContent(subTabs);
                        }
                    });

                    tabPane.getTabs().add(tab);
                }
            }
        }

        // Calculadora de Conversión (lazy)
        Tab calculadora = new Tab("Calculadora de Conversión");
        calculadora.setClosable(false);
        calculadora.setStyle("-fx-font-size: 16px;");
        calculadora.setContent(new Label("Cargando..."));
        calculadora.setOnSelectionChanged(e -> {
            if (calculadora.isSelected() && calculadora.getContent() instanceof Label) {
                calculadora.setContent(CalculadoraConversion.crearVista());
            }
        });
        tabPane.getTabs().add(calculadora);

        return tabPane;
    }
}
