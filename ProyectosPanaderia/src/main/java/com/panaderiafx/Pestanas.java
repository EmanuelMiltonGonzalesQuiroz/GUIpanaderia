package com.panaderiafx;

import com.panaderiafx.controllers.CalculadoraConversion;
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
        botonCambioVista.setStyle("-fx-background-color: #03A9F4; -fx-text-fill: white; -fx-padding: 6 12; -fx-font-weight: bold;");
        botonCambioVista.setOnAction(e -> {
            vistaBasica = !vistaBasica;
            String texto = vistaBasica ? "🔁 Cambiar a vista avanzada" : "🔁 Cambiar a vista básica";
            botonCambioVista.setText(texto);
            root.setCenter(generarVista(vistaBasica)); // Recarga el contenido
        });

        cabecera.setPadding(new Insets(10));
        cabecera.getChildren().add(botonCambioVista);
        root.setTop(cabecera);
        root.setCenter(generarVista(vistaBasica));

        return root;
    }

    private static TabPane generarVista(boolean vistaBasica) {
        TabPane tabPane = new TabPane();

        List<Map<String, String>> config = VerUtils.verTabla("ConfiguraciónTablas");
        Set<String> agregados = new HashSet<>();

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
                tab.setContent(new Label("Cargando..."));
                tab.setStyle("-fx-font-size: 16px;");

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

        if (!vistaBasica) {
            List<String> hojas = VerUtils.obtenerNombresTablas();
            for (String hoja : hojas) {
                if (!agregados.contains(hoja.toLowerCase())) {
                    Tab tab = new Tab(hoja);
                    tab.setClosable(false);
                    tab.setContent(new Label("Cargando..."));
                    tab.setStyle("-fx-font-size: 16px;");

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

        Tab calculadora = new Tab("Calculadora de Conversión");
        calculadora.setClosable(false);
        calculadora.setContent(CalculadoraConversion.crearVista());
        calculadora.setStyle("-fx-font-size: 16px;");
        tabPane.getTabs().add(calculadora);

        return tabPane;
    }
}
