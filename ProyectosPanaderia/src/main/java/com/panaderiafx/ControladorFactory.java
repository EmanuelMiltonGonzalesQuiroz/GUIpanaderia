package com.panaderiafx;

import javafx.scene.Node;
import com.panaderiafx.controllers.*;

public class ControladorFactory {

    public static Node getVista(String accion, String tabla, String nombreVisible) {
        switch (accion.toLowerCase()) {
            case "ver":
                return VerController.mostrar(tabla, nombreVisible);
            case "crear":
                return CrearController.mostrar(tabla, nombreVisible);
            case "modificar":
                return ModificarController.mostrar(tabla, nombreVisible);
            default:
                return new javafx.scene.control.Label("Sin contenido definido para: " + accion);
        }
    }
}
