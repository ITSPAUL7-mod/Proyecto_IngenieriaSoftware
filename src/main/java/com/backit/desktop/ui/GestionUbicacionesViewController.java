package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class GestionUbicacionesViewController {

    @FXML private TextField txtNombreUbicacion;
    @FXML private ComboBox<String> cmbTipoUbicacion;
    @FXML private ComboBox<String> cmbUbicacionPadre;
    @FXML private TextField txtHorarioAtencion;
    @FXML private TreeView<String> tvJerarquiaUbicaciones;

    @FXML
    public void initialize() {
        cmbTipoUbicacion.getItems().addAll("SEDE", "EDIFICIO", "PISO", "ZONA", "BODEGA");
        TreeItem<String> root = new TreeItem<>("Sede Central");
        root.getChildren().add(new TreeItem<>("Edificio A -> Piso 1 -> Bodega 101"));
        tvJerarquiaUbicaciones.setRoot(root);
    }

    @FXML
    private void handleGuardarUbicacion() {
        System.out.println("Ubicacion guardada.");
    }
}
