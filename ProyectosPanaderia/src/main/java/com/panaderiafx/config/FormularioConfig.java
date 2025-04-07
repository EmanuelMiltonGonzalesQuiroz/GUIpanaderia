package com.panaderiafx.config;

import java.util.*;

public class FormularioConfig {
    public static class Campo {
        public String nombre;
        public boolean autogenerado = false;
        public String tablaOpciones = null;
        public String columnaOpciones = null;

        public Campo(String nombre) {
            this.nombre = nombre;
        }

        public Campo(String nombre, boolean autogenerado) {
            this.nombre = nombre;
            this.autogenerado = autogenerado;
        }

        public Campo(String nombre, String tablaOpciones, String columnaOpciones) {
            this.nombre = nombre;
            this.tablaOpciones = tablaOpciones;
            this.columnaOpciones = columnaOpciones;
        }
    }

    public static Map<String, List<Campo>> camposPorTabla = new HashMap<>();

    static {
        camposPorTabla.put("Ingredientes", Arrays.asList(
                new Campo("codigo", true),
                new Campo("nombre"),
                new Campo("unidad", "Ingredientes", "Unidad"),
                new Campo("precio") // Local o Dólar seleccionable
        ));

        camposPorTabla.put("Recetas", Arrays.asList(
                new Campo("codigo", true),
                new Campo("nombre"),
                new Campo("ingrediente", "Ingredientes", "Nombre")
        ));

        camposPorTabla.put("Costos", Arrays.asList(
                new Campo("codigo", true),
                new Campo("descripcion"),
                new Campo("tipo"),
                new Campo("monto")
        ));

        camposPorTabla.put("TasaCambio", Arrays.asList(
                new Campo("codigo", true),
                new Campo("fecha"),
                new Campo("valor")
        ));

        camposPorTabla.put("TablaConversion", Arrays.asList(
                new Campo("codigo", true),
                new Campo("unidad_origen"),
                new Campo("unidad_destino"),
                new Campo("factor")
        ));
    }
}
