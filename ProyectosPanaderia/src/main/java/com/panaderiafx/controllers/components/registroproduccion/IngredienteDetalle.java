package com.panaderiafx.controllers.components.registroproduccion;

import javafx.beans.property.*;

public class IngredienteDetalle {
    private final StringProperty codigo;
    private final StringProperty cantidad;
    private final StringProperty unidad;
    private final StringProperty costo;
    private final BooleanProperty usar;

    public IngredienteDetalle(String codigo, String cantidad, String unidad, String costo, boolean usar) {
        this.codigo = new SimpleStringProperty(codigo);
        this.cantidad = new SimpleStringProperty(cantidad);
        this.unidad = new SimpleStringProperty(unidad);
        this.costo = new SimpleStringProperty(costo);
        this.usar = new SimpleBooleanProperty(usar);
    }

    public StringProperty codigoProperty() { return codigo; }
    public StringProperty cantidadProperty() { return cantidad; }
    public StringProperty unidadProperty() { return unidad; }
    public StringProperty costoProperty() { return costo; }
    public BooleanProperty usarProperty() { return usar; }
}
