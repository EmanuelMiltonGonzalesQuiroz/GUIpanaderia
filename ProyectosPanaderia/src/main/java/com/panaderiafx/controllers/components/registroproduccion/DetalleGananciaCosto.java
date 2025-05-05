package com.panaderiafx.controllers.components.registroproduccion;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class DetalleGananciaCosto {

    public static class ProduccionDetalle {
        private final String receta;
        private final double ganancia;
        private final double costoDirecto;
        private final CheckBox incluir;

        public ProduccionDetalle(String receta, double ganancia, double costoDirecto) {
            this.receta = receta;
            this.ganancia = ganancia;
            this.costoDirecto = costoDirecto;
            this.incluir = new CheckBox();
            this.incluir.setSelected(true);
        }

        public String getReceta() {
            return receta;
        }

        public double getGanancia() {
            return ganancia;
        }

        public double getCostoDirecto() {
            return costoDirecto;
        }

        public CheckBox getIncluir() {
            return incluir;
        }

        public boolean isSeleccionado() {
            return incluir.isSelected();
        }
    }

    public static Node generarTabla(List<ProduccionDetalle> detalles) {
        TableView<ProduccionDetalle> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setMaxWidth(550);

        TableColumn<ProduccionDetalle, String> colReceta = new TableColumn<>("Receta");
        colReceta.setCellValueFactory(new PropertyValueFactory<>("receta"));

        TableColumn<ProduccionDetalle, Double> colGanancia = new TableColumn<>("Ganancia");
        colGanancia.setCellValueFactory(new PropertyValueFactory<>("ganancia"));

        TableColumn<ProduccionDetalle, Double> colCosto = new TableColumn<>("Costo Directo");
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoDirecto"));

        TableColumn<ProduccionDetalle, CheckBox> colCheck = new TableColumn<>("✓");
        colCheck.setCellValueFactory(new PropertyValueFactory<>("incluir"));

        tabla.getColumns().addAll(colReceta, colGanancia, colCosto, colCheck);
        tabla.getItems().addAll(detalles);

        Label titulo = new Label("Detalle de Producciones");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        VBox contenedor = new VBox(10, titulo, tabla);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");

        return contenedor;
    }
}
