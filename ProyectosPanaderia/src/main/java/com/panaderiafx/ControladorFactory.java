package com.panaderiafx;

import javafx.scene.Node;
import com.panaderiafx.controllers.*;

public class ControladorFactory {

    public static Node getVista(String accion, String tabla) {
        switch (accion.toLowerCase()) {
            case "ver":
                return VerController.mostrar(tabla);
            case "crear":
                return CrearController.mostrar(tabla);
            case "modificar":
                return ModificarController.mostrar(tabla);
            case "eliminar":
                return EliminarController.mostrar(tabla);
            case "gráfico":
            case "grafico":
                return GraficoController.mostrar(tabla);
            default:
                return new javafx.scene.control.Label("Sin contenido definido para: " + accion);
        }
    }
}
