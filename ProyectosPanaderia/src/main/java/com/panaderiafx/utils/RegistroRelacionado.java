package com.panaderiafx.utils;

import java.util.Map;

public class RegistroRelacionado {
    public String tabla;
    public Map<String, String> datos;

    public RegistroRelacionado(String tabla, Map<String, String> datos) {
        this.tabla = tabla;
        this.datos = datos;
    }
}
